import com.github.yajuhua.extractor.yt.Playlist;
import com.github.yajuhua.extractor.yt.Video;
import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.testng.AssertJUnit.*;

public class PlaylistTest {
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
     * 第一个是最新的
     * @throws Exception
     */
    @Test
    public void getInitByDesc() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        List<VideoInfo> initList = Playlist.getInit(url, Video.Sorted.DESCByTime);
        assertFalse("无法获取playlist页面初始化页面视频列表", initList.isEmpty());
    }

    /**
     * 第一个是最旧的
     * @throws Exception
     */
    @Test
    public void getInitByASC() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        List<VideoInfo> initList = Playlist.getInit(url, Video.Sorted.ACSByTime);
        assertFalse("无法获取playlist页面初始化页面视频列表", initList.isEmpty());
    }

    /**
     * 初始页面默认的顺序
     * @throws Exception
     */
    @Test
    public void getInitByDefault() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        List<VideoInfo> initList = Playlist.getInit(url, Video.Sorted.Default);
        assertFalse("无法获取playlist页面初始化页面视频列表", initList.isEmpty());
    }

    /**
     * 获取更多视频(小于100)
     * @throws Exception
     */
    @Test
    public void getMoreLessThan100() throws Exception {
        //不到100个视频
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        List<VideoInfo> more = Playlist.getMore(url, Video.Sorted.DESCByTime);
        assertTrue("获取小于100出错",more.size() <= 100);
    }

    /**
     * 获取更多视频(大于100)
     * @throws Exception
     */
    @Test
    public void getMoreMoreThan100()throws Exception{
        //超过100个视频
        String url = "https://www.youtube.com/playlist?list=PL4URKQHTymln_XUnkjGqmn9cjEPrQFMNW";
        List<VideoInfo> more = Playlist.getMore(url, Video.Sorted.DESCByTime);
        assertTrue("获取小于100出错",more.size() > 100);
    }

    /**
     * 获取playlist页面信息
     * @throws Exception
     */
    @Test
    public void getInfo() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PL4URKQHTymln_XUnkjGqmn9cjEPrQFMNW";
        PageInfo pageInfo = Playlist.getInfo(url);
        assertNotNull("获取的pageInfo对象为null",pageInfo);
        assertNotNull("无法获取playlist名称",pageInfo.getName());
        assertNotNull("无法获取playlist封面",pageInfo.getImage());
        assertNotNull("无法获取playlist描述",pageInfo.getDescription());
        assertNotNull("无法获取playlistId",pageInfo.getId());
    }
}
