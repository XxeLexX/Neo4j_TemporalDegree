package labor.toys;

public class Main_convert {
    public static void main(String[] args) {
        String input = "5f1e97331ab2ca6a9edb0619;[5f1e972b71ff1426a2044bb9];5f1e973218ba2c6a66cd8ac6;5f1e97321ab2ca6a9ed986e3;Trip;15686|1|2014-04-05 11\\:09\\:10|2014-04-05 11\\:15\\:46|396|Subscriber|1970;(1595840299331,9223372036854775807),(1396696150000,1396696546000)";
        String dbPath = "/Users/lxx/Desktop/neo4jTest/src/main/resources/db_lables";

        StringToCypher toCypher = new StringToCypher();
        toCypher.convert(input, dbPath);

    }
}
