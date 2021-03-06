The codebase presented in this repository was developed between 2010 and 2012 and replaced by the Triplifier, FIMS, and BCID applications as the main BiSciCol products.  This code is preserved here since it contains many good ideas, some which were expanded on and others which remain here.  Following is the contents of the BiSciCol homepage as seen on the Google Code site:

-------

The BiSciCol codebase reads GUIDs that are linked to other GUIDs either through direct relations or sameAs expressions and infers relationships between them including children, parents, descendents, ancestors, and siblings.  A Loading package is included to take "triplified" datasets and index them within Virtuoso for use by the BiSciCol application (see the Triplifier Application).

Model Interfaces:
1) Virtuoso
2) File

Renderers:
1) Tree-based
2) Query by Type
3) HTML
4) Map

A REST interface is provided to query the codebase, upon which the BiSciCol interface is built.  For more information see the <a href="https://docs.google.com/document/d/1OFc9OkKM9wCCa2myRCQmiGLBK2y4Jcn9NNVbAeACY_s/edit?hl=en&authkey=CJK4hugF">BiSciCol Design Document</a>
