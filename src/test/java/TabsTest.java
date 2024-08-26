import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.tab.Tabs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

public class TabsTest {
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
     * 获取channel的tabs
     * @throws Exception
     */
    @Test
    public void getTabs() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        JsonArray tabs = Tabs.getTabs(url);
        assertNotNull("tabs为null",tabs);
        assertFalse("找不到tabs",tabs.isEmpty());
    }

    /**
     * 获取tabs的title
     * @throws Exception
     */
    @Test
    public void getTabNames() throws Exception {
        String url = "https://www.youtube.com/@laogao";
        JsonArray tabs = Tabs.getTabs(url);
        List<String> tabNames = Tabs.getTabNames(tabs);
        assertNotNull("tabName为null",tabNames);
        assertFalse("找不到tabs标题",tabNames.isEmpty());
    }

    /**
     * 获取视频列表
     * @throws Exception
     */
    @Test
    public void getVideoList() throws Exception {
        String url = "https://www.youtube.com/@laogao/videos";
        JsonArray tabs = Tabs.getTabs(url);
        for (JsonElement tab : tabs) {
            String tabName = Tabs.getTabName(tab.getAsJsonObject());
            if (tabName != null && tabName.equalsIgnoreCase("videos")){
                JsonObject tabRenderer = tab.getAsJsonObject()
                        .getAsJsonObject("tabRenderer");
                JsonObject richGridRenderer = Tabs.getRichGridRenderer(tabRenderer);
                if (richGridRenderer == null){
                    continue;
                }
                List<VideoInfo> videoList = Tabs.getVideoList(richGridRenderer);
                assertFalse("找不到视频",videoList.isEmpty());
                return;
            }
        }
    }

    /**
     * 获取视频列表 by url
     * @throws Exception
     */
    @Test
    public void getVideoListByUrl() throws Exception{
        String url1 = "https://www.youtube.com/@laogao/videos";
        List<VideoInfo> videos = Tabs.getVideoList(url1);
        assertFalse("找不到videos列表",videos.isEmpty());

        String url2 = "https://www.youtube.com/@laogao/streams";
        List<VideoInfo> streams = Tabs.getVideoList(url2);
        assertFalse("找不到streams列表",streams.isEmpty());
    }

}
