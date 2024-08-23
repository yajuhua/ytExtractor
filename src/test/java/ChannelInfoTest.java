import com.github.yajuhua.extractor.yt.ChannelInfo;
import com.github.yajuhua.extractor.yt.YtExtrator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.testng.AssertJUnit.assertNotNull;

public class ChannelInfoTest {

    /**
     * 设置代理
     */
    @BeforeTest
    public void setProxy(){
        Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",10809));
        YtExtrator.proxy = proxy;
    }

    /**
     * 测试频道信息 by @username
     */
    @Test
    public void testChannelPageByUserName() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        ChannelInfo channelInfo = ChannelInfo.getInfo(url);
        assertNotNull("获取的channelInfo对象为null",channelInfo);
        assertNotNull("无法获取频道名称",channelInfo.getName());
        assertNotNull("无法获取频道封面",channelInfo.getImage());
        assertNotNull("无法获取频道描述",channelInfo.getDescription());
    }

    /**
     * 测试频道信息 by Id
     */
    @Test
    public void testChannelPageById() throws Exception {
        String url = "https://www.youtube.com/channel/UCMUnInmOkrWN4gof9KlhNmQ";
        ChannelInfo channelInfo = ChannelInfo.getInfo(url);
        assertNotNull("获取的channelInfo对象为null",channelInfo);
        assertNotNull("无法获取频道名称",channelInfo.getName());
        assertNotNull("无法获取频道封面",channelInfo.getImage());
        assertNotNull("无法获取频道描述",channelInfo.getDescription());
    }

    /**
     * 测试playlist信息
     */
    @Test
    public void testPlaylistInfo() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        ChannelInfo channelInfo = ChannelInfo.getInfo(url);
        assertNotNull("获取的channelInfo对象为null",channelInfo);
        assertNotNull("无法获取playlist名称",channelInfo.getName());
        assertNotNull("无法获取playlist封面",channelInfo.getImage());
        assertNotNull("无法获取playlist描述",channelInfo.getDescription());
    }
}
