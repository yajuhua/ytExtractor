import com.github.yajuhua.extractor.yt.Channel;
import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.testng.AssertJUnit.assertNotNull;

public class ChannelTest {
    /**
     * 设置代理
     */
    @BeforeTest
    public void setProxy(){
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Win")){
            Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",10809));
            YtExtrator.proxy = proxy;
        }
    }

    /**
     * 获取channel页面信息
     * @throws Exception
     */
    @Test
    public void getInfo() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        PageInfo pageInfo = Channel.getInfo(url);
        assertNotNull("获取的pageInfo对象为null",pageInfo);
        assertNotNull("无法获取channel名称",pageInfo.getName());
        assertNotNull("无法获取channel封面",pageInfo.getImage());
        assertNotNull("无法获取channel描述",pageInfo.getDescription());
        assertNotNull("无法获取channelId",pageInfo.getId());
    }
}
