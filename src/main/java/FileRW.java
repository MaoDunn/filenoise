
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件读写
 * @author maodun
 * @Date 2019/2/1
 */
public class FileRW {
    /**
     * 读取文件内容到字节数组
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public byte[] readFiletoByte(String filePath) throws IOException {
        File file = new File(filePath);
        if (!(file.exists() && file.isFile())) {
            System.err.println("文件不存在");
            return null;
        }
        if (file.length() == 0) {
            System.err.println("文件内容不能为空");
            return null;
        }
        InputStream in = null;
        ByteArrayOutputStream byteOut = null;
        byte[] result = null;
        try {
            in = new FileInputStream(file);
            byteOut = new ByteArrayOutputStream();
            //buff用于存放循环读取的临时数据
            byte[] buff = new byte[(int) file.length()];
            int len;
            while ((len = in.read(buff)) != -1) {
                byteOut.write(buff, 0, len);
                result = byteOut.toByteArray();
            }
        } catch (IOException e) {
        } finally {
            try {
                if (byteOut != null)
                    byteOut.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 字节数组写入文件
     *
     * @param path
     * @param content
     * @throws IOException
     */
    public void writeByte(String path, byte[] content) throws IOException {
        OutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
    }
}
