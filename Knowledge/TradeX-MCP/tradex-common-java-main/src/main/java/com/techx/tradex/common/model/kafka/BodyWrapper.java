package com.techx.tradex.common.model.kafka;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;

@Data
@AllArgsConstructor
@JsonSerialize(using = BodyWrapper.Seralizer.class)
public class BodyWrapper implements Body {
    private Object realBody;

    @Override
    public String getPartitionKey() {
        if (realBody instanceof Body) {
            return ((Body) realBody).getPartitionKey();
        }
        return null;
    }

    @Override
    public String getMessageKey() {
        if (realBody instanceof Body) {
            return ((Body) realBody).getMessageKey();
        }
        return this.getClass().getSimpleName();
    }

    public static class Seralizer extends StdSerializer<BodyWrapper> {
        public Seralizer() {
            this(BodyWrapper.class);
        }

        public Seralizer(Class<BodyWrapper> t) {
            super(t);
        }

        @Override
        public void serialize(BodyWrapper value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(value.realBody);
        }
    }
}
