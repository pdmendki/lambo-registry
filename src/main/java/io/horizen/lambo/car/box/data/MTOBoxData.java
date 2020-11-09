package io.horizen.lambo.car.box.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.data.AbstractNoncedBoxData;
import com.horizen.box.data.NoncedBoxDataSerializer;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import io.horizen.lambo.car.box.MTOBox;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.serialization.Views;
import scorex.crypto.hash.Blake2b256;

import java.util.Arrays;

import static io.horizen.lambo.car.box.data.CarRegistryBoxesDataIdsEnum.MTOBoxDataId;

//MTOBoxData is mileage token data class that contains token holdings for a given account
@JsonView(Views.Default.class)
public final class MTOBoxData extends AbstractNoncedBoxData<PublicKey25519Proposition, MTOBox, MTOBoxData> {

    /**
     * Public key of account is used as id to ensure uniqueness
     * Account balance
     */
    private final String id;
    private final long balance;


    public MTOBoxData(PublicKey25519Proposition proposition, String id, long balance) {
        //Zen equivalent value is set to 1, Mileage token at moment is restricted to sidechain app only
        super(proposition, 1);
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }

    @Override
    public MTOBox getBox(long nonce) {
        return new MTOBox(this, nonce);
    }

    @Override
    public byte[] customFieldsHash() {
        return Blake2b256.hash(
                Bytes.concat(
                        id.getBytes(),
                        Longs.toByteArray(balance)
                ));
    }

    @Override
    public NoncedBoxDataSerializer serializer() {
        return null;
    }

    @Override
    public byte boxDataTypeId() {
        return MTOBoxDataId.id();
    }

    @Override
    public byte[] bytes() {
        return Bytes.concat(
                proposition().bytes(),
                Ints.toByteArray(id.getBytes().length),
                id.getBytes(),
                Longs.toByteArray(balance)
        );
    }

    public static MTOBoxData parseBytes(byte[] bytes) {
        int offset = 0;

        PublicKey25519Proposition proposition = PublicKey25519PropositionSerializer.getSerializer()
                .parseBytes(Arrays.copyOf(bytes, PublicKey25519Proposition.getLength()));
        offset += PublicKey25519Proposition.getLength();

        int size = Ints.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));
        offset += Ints.BYTES;

        String id = new String(Arrays.copyOfRange(bytes, offset, offset + size));
        offset += size;

        long balance = Longs.fromByteArray(Arrays.copyOfRange(bytes, offset, offset + Ints.BYTES));

        return new MTOBoxData(proposition, id, balance);
    }

    @Override
    public String toString() {
        return "MTOBoxData{" +
                "id=" + id +
                ", proposition=" + proposition() +
                ", balance=" + balance +
                '}';
    }
}

