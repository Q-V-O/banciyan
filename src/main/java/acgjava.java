import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.setting.dialect.Props;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class acgjava {
    //驱动
    static String drive;
    //保存地址
    static String download;

    static {
        Props props = new Props("config.properties");
        drive = props.getStr("drive");
        download = props.getStr("download");
    }

    //获取各个页面的链接
    public static void pa( String ul){
        String listContent = HttpUtil.get("https://www.bcy.net/apiv3/search/getContent?query="+ul+"&size=200");
        List<String> titles = ReUtil.findAll("item_id(.*?)uid", listContent, 1);
        for (int i = 0; i < titles.size(); i++){
            //上面数据不干净,这里去除标点符号
            titles.set(i,titles.get(i).replaceAll("[\\pP\\p{Punct}]", ""));
        }
        paurl(titles);
        System.out.println("爬取链接获取完毕, 开启获取图片链接......");
    }

    //获取各个图片的高清链接
    public static void paurl(List<String> zhi){
        String element = null;
        System.setProperty("webdriver.edge.driver",drive);
        for (int m=0; m< zhi.size(); m++){
            try {
                //用系统浏览器获取二次加载的数据
                WebDriver driver = new EdgeDriver();
                driver.get("https://www.bcy.net/item/detail/"+zhi.get(m));
                element = driver.findElement(By.className("album")).getAttribute("innerHTML");
                driver.close();
            } catch (Exception e) {
                System.err.println("出现未知连接错误......");
            }
            //拿到图片链接
            ArrayList<String> imgStr = getImgStr(element);
            for (int i = 0; i < imgStr.size(); i++){
                xiazai(imgStr.get(i),m+"-"+i+".jpg");
            }
        }
    }

    //获取图片字符串中所有链接
    public static ArrayList<String> getImgStr(String htmlStr) {
        ArrayList<String> pics = new ArrayList<>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        //     String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile
                (regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    //执行下载
    public static void xiazai(String fileUrl , String zhi){
        //将文件下载后保存在E盘，返回结果为下载文件大小
        long size = HttpUtil.downloadFile(fileUrl, FileUtil.file(download+zhi));
        System.out.println("名称: "+zhi+"   大小: " + size+"字节");
    }

    public static void main(String[] args) {
        System.out.print("输入搜索关键字:");
        pa(new Scanner(System.in).next());
    }
}