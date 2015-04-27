package BSCcore;

import org.joda.time.DateTime;

/**
 * This Interface can be implemented by classes calling various database engines.
 * It is expected that such classes also implement Configurable.
 * User: jdeck
 * Date: 5/31/12
 * Time: 10:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FilteredModel {
    BSCObject getBSCObject();

    BSCModel getSiblingsAsModel();

    BSCModel getSameAsModel();

    BSCModel getRelationsAsModel();

    BSCModel getAncestorsAsModel();

    BSCModel getDescendentsAsModel();

    QueryType getQueryType();

    void setQueryType(QueryType queryType);

    String getSubject();

    DateTime getDate();

    void setDateTime(DateTime date);

    void setSubject(String subject);

}
