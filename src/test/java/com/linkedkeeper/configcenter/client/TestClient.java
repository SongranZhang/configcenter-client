package com.linkedkeeper.configcenter.client;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class TestClient extends BaseTest {

    private ConfigCenterClient client;

    @Test
    public void test() {
        System.out.println("Test: start ...");
        try {
            Map<String, String> data = new HashMap<String, String>();
            for (int i = 0; i < 5; i++) {
                String key = "com.linkedkeeper.test." + i;
                String value = UUID.randomUUID().toString();
                data.put(key, value);
                client.set(key, value);
            }
            while (!data.isEmpty()) {
                for (Iterator<Map.Entry<String, String>> it = data.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, String> map = it.next();
                    String key = map.getKey();
                    String value = map.getValue();
                    String zkValue = client.get(key);
                    if (value.equals(zkValue)) {
                        it.remove();
                        System.out.println("Remove key -> {" + key + "}");
                    } else {
                        System.out.println("Diff key -> {" + key + "}, value = {" + map.getValue() + "}, zkValue = {" + zkValue + "}");
                    }
                }
                System.out.println("Still have count -> {" + data.size() + "}");
                Thread.sleep(6000);
            }
        } catch (Exception e) {
            System.err.print(e);
        }
    }

    public void setClient(ConfigCenterClient client) {
        this.client = client;
    }
}
