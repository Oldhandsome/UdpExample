package org.example.protocol.packet.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SimpleMessage extends Message {

    private String msg;

    @Override
    public int getMessageCode() {
        return SIMPLE_MSG;
    }
}
