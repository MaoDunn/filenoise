文件加噪、还原接口使用手册（Java）
1.	接口功能：
实现对文件的加噪和还原
2.	使用要求：
1.	文件大小在16字节以上
原因：当前实现方法前16字节的噪声为源信息，如文件大小不足16字节，无法完成源信息的存储
2.	膨胀率取值范围（0.125,0.25,0.5,1-15的整数）
原因：当前实现方法用一个字节存储膨胀率（与一个字节位数相乘之后的值），最大值为127，所以膨胀率最大值为15,1以内只能取与一个字节的位数（即8）相乘得整数的值，否则无法判断噪声数组什么时候向后移动
3.	接口方法：
/**
 * 文件加噪
 * @param filePath  文件路径
 * @param rate  膨胀率（取值范围0.125，0.25，0.5，1-15的整数）
 * @throws Exception
 */
public void addNoise(String filePath, double rate)throws Exception;
/**
 * 文件还原
 * @param filePath  文件路径
 * @throws Exception
 */
public void removeNoise (String filePath) throws Exception;
4.	接口调用测试代码示例：
public class FileOpTest {
    @Test
    public void test() {
        FileOpI fileOp = new FileOp();//实例化接口
        try {
            fileOp.addNoise("E:\\test.txt",0.5);//加噪
        	fileOp.removeNoise("E:\\test.txt");//还原
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
5.	辅助类说明:
1.	ConstantUtil类，常量类。用来存储初始化常量
/**
 * 常量类
 */
public class ConstantUtil {
    public final byte point = 8;//初始化插入点，取值范围0-8
    public final byte rate = 1;//初始化膨胀率，必须为1
    public final int blength = 8;//一个字节的位数
}
rate取值只能取膨胀率最小值，原因是加噪后字节数组大小是根据调用者传入膨胀率进行设置的，如果调用者传入膨胀率小于初始化膨胀率会造成数据丢失。为了方便，此处rate为和一个字节位数相乘之后的值（即0.125*8）
2.	SwitchMarkUtil类，符号位转换工具类。
通过逻辑运算，把一个字节的高N位置零，以获取字节的后N位。字节右移的时候需要用到，防止负数右移高位补符号位（即1）
3.	FileRW类，文件读写类。
该类包含把文件读取到字节数组，把字节数组写入文件两个方法，运用NIO的方法进行文件读取，在性能上优于传统的IO读取
FileChannel channel = null;
FileInputStream fs = null;
try {
    fs = new FileInputStream(file);
    channel = fs.getChannel();
    ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
    while ((channel.read(byteBuffer)) > 0) {
    }
    return byteBuffer.array();
4.	RandomUtil噪声工具类。
通过调用SecureRandom生成随机数
