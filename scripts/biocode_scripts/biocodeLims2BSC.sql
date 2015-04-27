#
# Tissues
#
# These are defined by the core Biocode Database.  Don't need to define these OR relatedTo/sameAs if I use the biocode prefix in the
# relatedTo section following

#
# Extractions
#

SELECT concat('<http://biocode.berkeley.edu/lims/',trim(e.extractionId),'> a <http://biscicol.org/biscicol.rdf#Extraction> .') as rdf
    FROM extraction as e
    WHERE trim(e.extractionId) != '' || e.extractionId != null

UNION

SELECT concat('<http://biocode.berkeley.edu/lims/',trim(e.extractionId),'> <http://purl.org/dc/terms/modified> "',e.date,'" .') as rdf
    FROM extraction as e
    WHERE (e.date != null) AND (e.extractionId != 'NULL' || e.extractionId != null || trim(e.extractionId) != '')

UNION

#
# Sequence
#

SELECT concat('<http://biocode.berkeley.edu/assembly/',a.id,'> a <http://biscicol.org/biscicol.rdf#Sequence> .') as rdf
    FROM assembly as a
    WHERE a.id != null

UNION

SELECT concat('<http://biocode.berkeley.edu/assembly/',a.id,'> <http://purl.org/dc/terms/modified> "',a.date,'" .') as rdf
    FROM assembly as a
    WHERE (a.date != null) AND (a.id != null)

UNION


#
# Relations
#

SELECT concat('<http://biocode.berkeley.edu/tissues/',trim(e.sampleId),'> <http://biscicol.org/biscicol.rdf#relatedTo> <http://biocode.berkeley.edu/lims/',trim(e.extractionId), '> .') as rdf
    FROM extraction as e
    WHERE (e.sampleId != null || trim(e.sampleId) != '') AND (e.extractionId != null || trim(e.extractionId) != '')


UNION

SELECT concat('<http://biocode.berkeley.edu/lims/',trim(a.extraction_Id),'> <http://biscicol.org/biscicol.rdf#relatedTo> <http://biocode.berkeley.edu/assembly/',a.id, '> .') as rdf
    FROM assembly as a
    WHERE (a.extraction_id != null || trim(a.extraction_id) != '') AND (a.id != null)

INTO OUTFILE "/tmp/biocodelims.n3";
