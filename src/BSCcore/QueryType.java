package BSCcore;

public enum QueryType {
	descendents,
	ancestors,
	relations,
	siblings;
	
	public static QueryType valueOfWithDefault(String name) {
		try {
			return valueOf(name);
		}
		catch (Exception e) {
			return descendents;
		}
	}	
}
