/*
Select 
  ns.lex as subject,
  np.lex as predicate,
  no.lex as object 
From triples t
Join nodes ns On (ns.id = t.s)
Join nodes np On (np.id = t.p)
Join nodes no On (no.id = t.o)
Limit 10;
*/
WITH RECURSIVE search_graph(o,s,p,depth) as (
    SELECT t.o,t.s,t.p,1
        FROM triples t,nodes n
        WHERE t.s  <10
    UNION 
    SELECT t.o, t.s, t.p, sg.depth + 1
        FROM triples t, search_graph sg
        WHERE t.s = sg.o 
            AND sg.depth > 2
)
SELECT 
    ns.lex as subject, 
    np.lex as predicate,
    no.lex as object,
    sg.depth
FROM search_graph sg
Join nodes ns On (ns.id = sg.s)
Join nodes np On (np.id = sg.p)
Join nodes no On (no.id = sg.o);
