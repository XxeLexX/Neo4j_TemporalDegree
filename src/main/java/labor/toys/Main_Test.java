package labor.toys;

import labor.nodeRel.DegreeTypeRel;
import labor.nodeRel.DimensionTypeRel;

public class Main_Test {
    public static void main(String[] args) {
        String dbPath = "/Users/lxx/Desktop/neo4jTest/src/main/resources/db";
        EmbeddedNeo4jTest neo4jTest = new EmbeddedNeo4jTest();
        DegreeTypeRel degreeType = DegreeTypeRel.IN;
        DimensionTypeRel dimensionType = DimensionTypeRel.VALID_TIME;
        neo4jTest.startNeo4j(dbPath, degreeType, dimensionType);
    }

}
