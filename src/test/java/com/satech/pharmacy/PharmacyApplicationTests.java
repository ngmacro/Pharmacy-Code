package com.satech.pharmacy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class PharmacyApplicationTests {

    @Value("${satech.mina.server.port}")
    private int port;

    @Autowired
    private MockMvc mockMvc;

   // @Test
    public void contextLoads() {
    }

  //  @Test
    public void testSocketConnection() {
        final int maxThreadCount = 3;
        final int maxMessageCount = 500;
        ExecutorService executor = Executors.newFixedThreadPool(maxMessageCount);

        for (int i = 0; i < maxThreadCount; i++) {
            executor.execute(new MessageSender(maxMessageCount));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

    }

   // @Test
    public void testController() throws Exception {

        String boxId = "111111";
        String stations="1010";

        mockMvc.perform(MockMvcRequestBuilders.get("/box/update/" + boxId +"/" + stations)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));


    }

    class MessageSender implements Runnable {

        private final int maxMessageCount;

        public MessageSender(int maxMessageCount) {
            this.maxMessageCount = maxMessageCount;
        }

        @Override
        public void run() {

            Socket client;
            try {
                client = new Socket("localhost", port);
                OutputStream outputStream = client.getOutputStream();

                for (int i = 0; i < maxMessageCount; i++) {
                    outputStream.write("Test".getBytes());
                    outputStream.flush();
                    Thread.sleep(50L);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
