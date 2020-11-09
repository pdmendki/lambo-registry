package io.horizen.lambo.car.box.data;

import com.horizen.box.data.NoncedBoxDataSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public final class MTOBoxDataSerializer implements NoncedBoxDataSerializer<MTOBoxData> {
    private static final MTOBoxDataSerializer serializer = new MTOBoxDataSerializer();

    private MTOBoxDataSerializer() {
        super();
    }

    public static MTOBoxDataSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(MTOBoxData boxData, Writer writer) {
        writer.putBytes(boxData.bytes());
    }

    @Override
    public MTOBoxData parse(Reader reader) {
        return MTOBoxData.parseBytes(reader.getBytes(reader.remaining()));
    }
}
