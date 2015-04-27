# SAMPLE SCRIPT TO DUMP BIOCODE DATA AS N3 for BISCICOL
#
# Define the Prefixes
#

SELECT '@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .' as rdf
UNION
SELECT '@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . ' as rdf
UNION
SELECT '@prefix owl: <http://www.w3.org/2002/07/owl#> . ' as rdf
UNION
SELECT '@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> . ' as rdf
UNION


#
# Define the Events
#

SELECT concat('<',e.guid,'> a <http://rs.tdwg.org/dwc/terms/Event> .') as rdf
    FROM biocode_collecting_event as e
    WHERE e.eventid != 'NULL' || e.eventid != null

UNION

#  Event Literals
SELECT
    concat('<',e.guid, '> <http://purl.org/dc/terms/modified> "',e.dateLastModified,'" .') as rdf
    FROM biocode_collecting_event as e
    WHERE (e.dateLastModified != 'NULL' || e.dateLastModified != null) AND (e.eventid != 'NULL' || e.eventid != null)


UNION

#
# Define a geographic Object
#

SELECT concat('<http://www.w3.org/2003/01/geo/wgs84_pos#',
        e.decimallatitude,
        ',',
        e.decimallongitude,
        if(maxerrorinmeters,concat(';u=',cast(MaxErrorInMeters as signed)),''),
        if(strcmp(horizontaldatum,"NULL"),concat(';crs=',horizontaldatum),''),
        '> a ',
        '<http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing> .'
    ) as rdf
    FROM biocode_collecting_event as e
    WHERE (e.eventid != 'NULL' || e.eventid != null) AND
    (e.decimallatitude != null || e.decimallatitude != 'NULL') AND
    (e.decimallongitude != null || e.decimallongitude != 'NULL')

UNION

#
# Define the Specimens
#

SELECT concat('<',b.guid, '> a <http://rs.tdwg.org/dwc/terms/MaterialSample> .') as rdf
    FROM biocode as b
    WHERE b.bnhm_id != 'NULL' || b.bnhm_id != null

UNION

SELECT concat('<',b.guid, '> <http://purl.org/dc/terms/modified> "',b.dateLastModified,'" .') as rdf
    FROM biocode as b
    WHERE b.dateLastModified != 'NULL' || b.dateLastModified != null

UNION

# Declare the owl:sameAs with Florida Numbers
select concat('<',b.guid,'> owl:sameAs <http://collections.flmnh.ufl.edu/collection_objects/alias/',vouchercatalognumber, '.rdf> .') as rdf
    FROM biocode as b
   WHERE (b.specimen_num_collector != 'NULL' || b.specimen_num_collector != null)
   AND vouchercatalognumber like 'UF%'

UNION

#
# Define the Tissues
#

SELECT concat('<',t.guid,'> a <http://rs.tdwg.org/dwc/terms/MaterialSample> . ') as rdf
    FROM biocode_tissue_view as t
    WHERE t.tissue_id != 'NULL' || t.tissue_id != null

UNION

SELECT concat('<',t.guid,'> <http://purl.org/dc/terms/modified> "',t.dateLastModified,'" .') as rdf
    FROM biocode_tissue_view as t
    WHERE (t.dateLastModified != 'NULL' || t.dateLastModified != null) AND (t.tissue_id!= 'NULL' || t.tissue_id != null)

UNION

#
# Define the Extractions
#
#
#SELECT concat('<http://biocode.berkeley.edu/extractions/',e.extract_barcode,'> a <http://rs.tdwg.org/dwc/terms/MaterialSample> .') as rdf
#    FROM biocode_extract as e
#     WHERE e.extract_barcode != 'NULL' || e.extract_barcode != null
#
#UNION
#
#SELECT concat('<http://biocode.berkeley.edu/extractions/',e.extract_barcode,'> <http://purl.org/dc/terms/modified> "',e.dateLastModified,'" .') as rdf
#    FROM biocode_extract as e
#    WHERE (e.dateLastModified != 'NULL' || e.dateLastModified != null) AND (e.extract_barcode!= 'NULL' || e.extract_barcode != null)
#
#UNION

#
# Relations
#

# specimen depends_on event
SELECT concat('<',b.guid,'> <http://biscicol.org/terms/biscicol.owl#depends_on> <',e.guid, '> .') as rdf
    FROM biocode as b,biocode_collecting_event as e
    WHERE b.coll_eventid = e.eventid

UNION

# tissue derives_from specimen
SELECT concat('<',t.guid,'> < http://www.obofoundry.org/ro/ro.owl#derives_from> <',b.guid,'> .') as rdf
    FROM biocode as b,biocode_tissue_view as t
    WHERE b.bnhm_id = t.bnhm_id

UNION

# TODO: create a tissue -> dna extract relationship
# Biocode_extract not reliable here

SELECT concat('<',e.guid,'> <http://biscicol.org/terms/biscicol.owl#related_to> <http://www.w3.org/2003/01/geo/wgs84_pos#',
        e.decimallatitude,
        ',',
        e.decimallongitude,
        if(maxerrorinmeters,concat(';u=',cast(MaxErrorInMeters as signed)),''),
        if(strcmp(horizontaldatum,"NULL"),concat(';crs=',horizontaldatum),''),
        '> .'
    ) as rdf
    FROM biocode_collecting_event as e
    WHERE (e.eventid != 'NULL' || e.eventid != null) AND
    (e.decimallatitude != null || e.decimallatitude != 'NULL') AND
    (e.decimallongitude != null || e.decimallongitude != 'NULL')

INTO OUTFILE "/tmp/biocode.n3";
