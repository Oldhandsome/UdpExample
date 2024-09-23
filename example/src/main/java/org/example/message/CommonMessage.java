package org.example.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommonMessage extends BaseMessage {

    private String msg;

    @Override
    public int getCode() {
        return COMMON_MSG;
    }
}
