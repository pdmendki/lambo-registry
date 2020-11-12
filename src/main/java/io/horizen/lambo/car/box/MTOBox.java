package io.horizen.lambo.car.box;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.horizen.box.AbstractNoncedBox;
import com.horizen.box.BoxSerializer;
import io.horizen.lambo.car.box.data.MTOBoxData;
import io.horizen.lambo.car.box.data.MTOBoxDataSerializer;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.serialization.Views;

import java.util.Arrays;

import static io.horizen.lambo.car.box.CarRegistryBoxesIdsEnum.MTOBoxId;

// Declare default JSON view for MTOBox object. Will automatically collect all getters except ignored ones.
// New MTOBox will be created on declareCar if the account doesn't have mtoBox
// TODO - add new MTOBox in buyCar transaction as well if account doesn't have any
@JsonView(Views.Default.class)
@JsonIgnoreProperties({"mtoId", "value"})
public class MTOBox extends AbstractNoncedBox<PublicKey25519Proposition, MTOBoxData, MTOBox> {

    public MTOBox(MTOBoxData boxData, long nonce) {
        super(boxData, nonce);
    }

    @Override
    public BoxSerializer serializer() {
        return MTOBoxSerializer.getSerializer();
    }

    @Override
    public byte boxTypeId() {
        return MTOBoxId.id();
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                Longs.toByteArray(nonce),
                MTOBoxDataSerializer.getSerializer().toBytes(boxData)
        );
    }

    public static MTOBox parseBytes(byte[] bytes) {
        long nonce = Longs.fromByteArray(Arrays.copyOf(bytes, Longs.BYTES));
        MTOBoxData boxData = MTOBoxDataSerializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, Longs.BYTES, bytes.length));

        return new MTOBox(boxData, nonce);
    }

    public String getId(){
        return  boxData.getId();
    }

    public long getBalance(){
        return boxData.getBalance();
    }

}
