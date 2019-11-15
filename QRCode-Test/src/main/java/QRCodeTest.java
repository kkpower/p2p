import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName:QRCodeTest
 * Package:PACKAGE_NAME
 * Description:
 *
 * @date:2019/10/29 16:02
 * @author:guoxin
 */
public class QRCodeTest {

    public static void main(String[] args) throws WriterException, IOException {

        Map<EncodeHintType,Object> hintTypeObjectMap = new HashMap<EncodeHintType, Object>();
        hintTypeObjectMap.put(EncodeHintType.CHARACTER_SET,"UTF-8");

        //创建一个矩阵对象
        BitMatrix bitMatrix = new MultiFormatWriter().encode("weixin://wxpay/bizpayurl?pr=QcUIXcV", BarcodeFormat.QR_CODE,200,200,hintTypeObjectMap);

        String filePath = "D://";
        String fileName = "wx.jpg";

        Path path = FileSystems.getDefault().getPath(filePath,fileName);

        //将矩阵对象转换为二维码图片
        MatrixToImageWriter.writeToPath(bitMatrix,"jpg",path);

        System.out.println("成功");

    }
}
