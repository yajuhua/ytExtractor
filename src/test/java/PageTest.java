import com.github.yajuhua.extractor.yt.Page;
import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.*;

public class PageTest {
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
     * 获取playlist页面的信息
     */
    @Test
    public void playlistInfo() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        PageInfo pageInfo = Page.getInfo(url);
        assertNotNull("pageInfo为Null",pageInfo);
        assertNotNull("无法获playlist名称",pageInfo.getName());
        assertNotNull("无法获playlist封面",pageInfo.getImage());
        assertNotNull("无法获playlist描述",pageInfo.getDescription());
        assertNotNull("无法获playlist描述",pageInfo.getId());
    }

    /**
     * 获取playlist列表最新视频 小于100的
     * @throws Exception
     */
    @Test
    public void playlistLatestLessThan100() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        VideoInfo videoInfo = Page.getLatest(url);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取playlist列表最新视频 大于于100的
     * @throws Exception
     */
    @Test
    public void playlistLatestMoreThan100() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PL4URKQHTymln_XUnkjGqmn9cjEPrQFMNW";
        VideoInfo videoInfo = Page.getLatest(url);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取playlist列表最近几集
     * @throws Exception
     */
    @Test
    public void playlistRecent() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PL4URKQHTymln_XUnkjGqmn9cjEPrQFMNW";
        List<VideoInfo> recent = Page.getRecent(url, 2);
        assertTrue("获取最近两集失败",recent.size() == 2);
    }

    /**
     * playlist自定义剧集
     */
    @Test
    public void playlistCustomEs() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PL4URKQHTymln_XUnkjGqmn9cjEPrQFMNW";
        List<VideoInfo> videoList = Page.getVideoList(url, Arrays.asList(2, 5));
        assertNotNull("playlist获取自定义剧集为Null",videoList);
        assertFalse("playlist自定义剧集获取为空",videoList.isEmpty());
    }

    /**
     * 获取channel页面的信息
     */
    @Test
    public void channelInfo() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        PageInfo pageInfo = Page.getInfo(url);
        assertNotNull("pageInfo为Null",pageInfo);
        assertNotNull("无法获channel名称",pageInfo.getName());
        assertNotNull("无法获channel封面",pageInfo.getImage());
        assertNotNull("无法获channel描述",pageInfo.getDescription());
        assertNotNull("无法获channel描述",pageInfo.getId());
    }

    /**
     * 获取videos列表最新视频
     * @throws Exception
     */
    @Test
    public void channelVideosLatest() throws Exception {
        String url = "https://www.youtube.com/@laogao/videos";
        VideoInfo videoInfo = Page.getLatest(url);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频描述",videoInfo.getDescription());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取streams列表最近几集
     * @throws Exception
     */
    @Test
    public void channelVideosRecent() throws Exception {
        String url = "https://youtube.com/@Apple/videos";
        List<VideoInfo> recent = Page.getRecent(url, 2);
        assertTrue("channelVideoss获取最近两集失败",recent.size() == 2);
    }

    /**
     * channelVideos自定义剧集
     */
    @Test
    public void channelVideosCustomEs() throws Exception {
        String url = "https://www.youtube.com/@Apple/videos";
        List<VideoInfo> videoList = Page.getVideoList(url, Arrays.asList(2, 5));
        assertNotNull("channelVideos获取自定义剧集为Null",videoList);
        assertFalse("channelVideos自定义剧集获取为空",videoList.isEmpty());
    }

    /**
     * 获取streams列表最新视频
     * @throws Exception
     */
    @Test
    public void channelStreamsLatest() throws Exception {
        String url = "https://www.youtube.com/@Apple/streams";
        VideoInfo videoInfo = Page.getLatest(url);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频描述",videoInfo.getDescription());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取streams列表最近几集
     * @throws Exception
     */
    @Test
    public void channelStreamsRecent() throws Exception {
        String url = "https://youtube.com/@Apple/streams";
        List<VideoInfo> recent = Page.getRecent(url, 2);
        assertTrue("channelStreams获取最近两集失败",recent.size() == 2);
    }

    /**
     * channelStreams自定义剧集
     */
    @Test
    public void channelStreamsCustomEs() throws Exception {
        String url = "https://www.youtube.com/@Apple/streams";
        List<VideoInfo> videoList = Page.getVideoList(url, Arrays.asList(2, 5));
        assertNotNull("channelStreams获取自定义剧集为Null",videoList);
        assertFalse("channelStreams自定义剧集获取为空",videoList.isEmpty());
    }
}
