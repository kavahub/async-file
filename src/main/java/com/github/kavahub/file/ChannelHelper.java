package com.github.kavahub.file;

import java.io.IOException;
import java.nio.channels.Channel;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
@UtilityClass
public class ChannelHelper {
    /**
     * 
     * @param channel
     */
    public void close(Channel channel) {
        if (log.isDebugEnabled()) {
            log.debug("Close channel: {}", channel);
        }
        
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (IOException e) {
            log.error("Channel close failed", e);
        }
    }
}
