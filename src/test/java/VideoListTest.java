import com.github.yajuhua.extractor.yt.VideoList;
import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.testng.AssertJUnit.*;

/**
 * 测试 videos / streams / playlist 视频列表
 */
public class VideoListTest {

    /**
     * 设置代理
     */
    @BeforeTest
    public void setProxy(){
        Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",10809));
        YtExtrator.proxy = proxy;
    }

    /**
     * 获取channel/videos页面视频列表
     */
    @Test
    public void testGetVideosList() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        List<VideoInfo> videoInfoList = VideoList.get(url);
        assertTrue("无法获取到视频",!videoInfoList.isEmpty());
        VideoInfo videoInfo = videoInfoList.get(videoInfoList.size() - 1);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频描述",videoInfo.getDescription());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取channel/streams页面视频列表
     */
    @Test
    public void testGetStreamsList() throws Exception {
        String url = "https://www.youtube.com/@aottergirls/streams";
        List<VideoInfo> videoInfoList = VideoList.get(url);
        assertTrue("无法获取到视频",!videoInfoList.isEmpty());
        VideoInfo videoInfo = videoInfoList.get(videoInfoList.size() - 1);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频描述",videoInfo.getDescription());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }

    /**
     * 获取playlist视频列表
     */
    @Test
    public void testGetPlaylist() throws Exception {
        String url = "https://www.youtube.com/playlist?list=PLMigSoFRkQUl-vLVtYIWNYst0YibGl1VX";
        List<VideoInfo> videoInfoList = VideoList.get(url);
        assertTrue("无法获取到视频",!videoInfoList.isEmpty());
        VideoInfo videoInfo = videoInfoList.get(videoInfoList.size() - 1);
        assertNotNull("视频信息为Null",videoInfo);
        assertNotNull("无法获取视频标题",videoInfo.getTitle());
        assertNotNull("无法获取视频封面",videoInfo.getImage());
        assertNotNull("无法获取视频id",videoInfo.getId());
        assertNotNull("无法获取视频链接",videoInfo.getUrl());
        assertNotNull("无法获取视频时长",videoInfo.getDuration());
    }
}
