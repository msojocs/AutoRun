package com.example.autorun;

import org.runrun.App;
import org.runrun.entity.Location;
import org.runrun.entity.Response;
import org.runrun.utils.FileUtil;
import org.runrun.utils.JsonUtils;
import org.runrun.utils.TrackUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public  void genTack() throws IOException {
        long distance = 3200;
        InputStream resourceAsStream = App.class.getResourceAsStream("/map2.json");
        String json = FileUtil.ReadFile(resourceAsStream);
        if (json.length() == 0) {
            System.out.println("配置读取失败");
            return ;
        }
        Location[] locations = JsonUtils.string2Obj(json, Location[].class);
        String tracks = TrackUtils.gen(distance, locations);

        resourceAsStream.close();

        resourceAsStream = App.class.getResourceAsStream("/template.json");
        json = FileUtil.ReadFile(resourceAsStream);
        Response<Map> response = JsonUtils.string2Obj(json, new TypeReference<Response<Map>>() {
        });
        response.getResponse().put("trackPoint", tracks);

        FileUtil.WriteFile("D:\\Work\\unirun\\tracks.json", JsonUtils.obj2String(response));
    }
}