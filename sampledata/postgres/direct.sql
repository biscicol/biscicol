/* GET Relationship Operators */
/*
SELECT id,lex 
FROM nodes 
WHERE lex = 'http://biscicol.org/terms/index.html#depends_on'
OR lex ='http://biscicol.org/terms/index.html#related_to'
OR lex = 'http://biscicol.org/terms/index.html#alias_of'
OR lex = 'http://www.obofoundry.org/ro/ro.owl#derives_from';
SELECT
    ns.lex as subject,
    ns.id as idSubject,
    np.lex as predicate,
    np.id as idPredicate,
    no.lex as object,
    no.id as idObject
FROM quads t
    Join nodes ns On (ns.id = t.s)
    Join nodes np On (np.id = t.p)
    Join nodes no On (no.id = t.o)
WHERE 
    t.o = 12008 AND
    (t.p = 5 OR t.p = 10241 OR t.p = 1724682);
*/

/* Find the related object(s) from above query (e.g. o = 6) and LOOP level 2 relationships */
SELECT
    ns.lex as subject,
    ns.id as idSubject,
    np.lex as predicate,
    np.id as idPredicate,
    no.lex as object,
    no.id as idObject
FROM quads t
    Join nodes ns On (ns.id = t.s)
    Join nodes np On (np.id = t.p)
    Join nodes no On (no.id = t.o)
WHERE 
    ns.lex = 'ark:/21547/Aa2_C200139E4AE8FE2346AE' AND
    (t.p = 5 OR t.p = 10241 OR t.p = 1724682)
LIMIT 10;

SELECT
    ns.lex as subject,
    ns.id as idSubject,
    np.lex as predicate,
    np.id as idPredicate,
    no.lex as object,
    no.id as idObject
FROM quads t
    Join knodes ns On (ns.id = t.s)
    Join knodes np On (np.id = t.p)
    Join knodes no On (no.id = t.o)
WHERE 
    no.lex = 'ark:/21547/Aa2_C200139E4AE8FE2346AE'
   AND (t.p = 10241 OR t.p = 5 OR t.p = -999999 OR t.p = 2139043) LIMIT 10;
