package es.um.asio.service.test;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class Test_1 {

    @Test
    public void test1() throws URISyntaxException {

        String mydata = "some string with 'the data i want' inside";
        Pattern pattern = Pattern.compile("'(.*?)'");
        Matcher matcher = pattern.matcher(mydata);
        if (matcher.find())
        {
            System.out.println(matcher.group(1));
        }
        System.out.println();

/*        String [][] m = null;
        m = addRow(m, new String[] {"1","1","1"});
        m = addRow(m, new String[] {"2","2","2"});
        m = addRow(m, new String[] {"3","3","3"});
        System.out.println();
        m = deleteRow(m,"2");
        m = deleteRow(m,"1");
        m = deleteRow(m,"3");
        System.out.println();
        m = addRow(m, new String[] {"1","1","1"});
        m = addRow(m, new String[] {"2","2","2"});
        m = addRow(m, new String[] {"3","3","3"});
        System.out.println();*/
    }

    public String[][] addRow(String[][] m, String[] row) {
        String[][] mAux;
        if (m==null || m.length == 0) {
            mAux = new String[1][row.length]; // Creo la matriz con el numero de columnas del vector

        } else {
            mAux = new String[m.length + 1][m[0].length];
            for (int i = 0; i < m.length; i++) { // Filas
                for (int j = 0; j < m[i].length; j++) { // Columnas
                    mAux[i][j] = m[i][j];
                }
            }

        }
        for (int i = 0; i < row.length; i++) {
            mAux[mAux.length - 1][i] = row[i];
        }
        return mAux;
    }

    public String[][] deleteRow(String[][] m, String codProducto) {
        int index = getRowIndexByProductCode(m,codProducto);
        if (index<0)
            return m;
        else {
            String[][] mAux;
            mAux = new String[m.length - 1][m[0].length];
            for (int i = 0; i < m.length; i++) { // Filas
                if (i == index)
                    continue;
                int fIndex;
                if (i>index)
                    fIndex = i-1;
                else
                    fIndex = i;
                for (int j = 0; j < m[fIndex].length; j++) { // Columnas
                    mAux[fIndex][j] = m[i][j];
                }
            }
            return mAux;
        }
    }

    public int getRowIndexByProductCode(String[][] m, String codProducto) {

        for (int i = 0; i < m.length; i++) { // Filas
                if (m[i][0].equals(codProducto))
                    return i;
        }
        return -1;
    }
}
