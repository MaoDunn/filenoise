import org.junit.Test;


/**
 * @author maodun
 * @Date 2021/12/17
 */
public class TestUtil {

    FileOpI fileOpI = new FileOp();

    @Test
    public void testAddNoise() throws Exception {
        String filePath = "E:\\test4\\chaoshi.json";
        double rate = 0.5;
        fileOpI.addNoise(filePath,rate);
    }

    @Test
    public void testRemoveNoise() throws Exception {
        String filePath = "E:\\test4\\chaoshi.json";
        fileOpI.removeNoise(filePath);
    }
}
