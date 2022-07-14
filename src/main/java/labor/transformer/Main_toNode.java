package labor.transformer;

import labor.nodeRel.DegreeTypeRel;
import labor.nodeRel.DimensionTypeRel;

public class Main_toNode {
    public static void main(String[] args) {
        DegreeTypeRel degreeType = DegreeTypeRel.IN;
        DimensionTypeRel dimensionType = DimensionTypeRel.VALID_TIME;

        String csvPath = "/Users/lxx/Desktop/Data_temporal/citibike_edges_1.csv";
        String dbPath = "/Users/lxx/Desktop/neo4jTest/src/main/resources/db_toNodes";

        ToNode toNode = new ToNode(degreeType, dimensionType);
        toNode.stringToNode(csvPath, dbPath);
    }
}
