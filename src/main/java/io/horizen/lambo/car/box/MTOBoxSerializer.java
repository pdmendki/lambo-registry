package io.horizen.lambo.car.box;

import com.horizen.box.BoxSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class MTOBoxSerializer implements BoxSerializer<MTOBox> {

    private static final MTOBoxSerializer serializer = new MTOBoxSerializer();

    private MTOBoxSerializer() {
        super();
    }

    public static MTOBoxSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(MTOBox box, Writer writer) {
        writer.putBytes(box.bytes());
    }

    @Override
    public MTOBox parse(Reader reader) {
        return MTOBox.parseBytes(reader.getBytes(reader.remaining()));
    }
}
