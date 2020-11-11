package io.horizen.lambo.car.transaction;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.horizen.box.BoxUnlocker;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Proof;
import com.horizen.proof.Signature25519;
import com.horizen.proof.Signature25519Serializer;
import com.horizen.proposition.Proposition;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.proposition.PublicKey25519PropositionSerializer;
import com.horizen.transaction.TransactionSerializer;
import com.horizen.utils.BytesUtils;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.MTOBox;
import io.horizen.lambo.car.box.data.MTOBoxData;
import io.horizen.lambo.car.info.CarBuyOrderInfo;
import scorex.core.NodeViewModifier$;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.horizen.lambo.car.transaction.CarRegistryTransactionsIdsEnum.EarnTokenTransactionId;

// EarnTokenTransaction is nested from AbstractRegularTransaction so support regular coins transmission as a part of fees.
// It is designed to earn Mileage Tokens - MTO.
// As outputs it contains possible RegularBoxes(to pay fee and change) and new MTOBox entry.
// As unlockers it contains MTOBox and RegularBoxes to open.
public class EarnTokenTransaction extends AbstractRegularTransaction{
    public EarnTokenTransaction(List<byte[]> inputRegularBoxIds, List<Signature25519> inputRegularBoxProofs, List<RegularBoxData> outputRegularBoxesData, long fee, long timestamp, MTOBox tokenBox, Signature25519 tokenBoxOwnershipProof, PublicKey25519Proposition ownerProposition, long amount) {
        super(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData, fee, timestamp);
        this.tokenBox = tokenBox;
        this.tokenBoxOwnershipProof = tokenBoxOwnershipProof;
        this.ownerProposition = ownerProposition;
        this.amount = amount;
    }

    private final MTOBox tokenBox;    // MTOBox to be updated
    private final Signature25519 tokenBoxOwnershipProof;   // Proof to unlock the MTOBox
    private final PublicKey25519Proposition ownerProposition; // Owner of the MTOBox.
    private final long amount; //amount of tokens to be added
    private List<NoncedBox<Proposition>> newBoxes;

    @Override
    public byte transactionTypeId() {
        return EarnTokenTransactionId.id();
    }

    // Override unlockers to contains regularBoxes from the parent class appended with MTOBox entry to be opened.
    @Override
    public List<BoxUnlocker<Proposition>> unlockers() {
        // Get Regular unlockers from base class.
        List<BoxUnlocker<Proposition>> unlockers = super.unlockers();

        BoxUnlocker<Proposition> unlocker = new BoxUnlocker<Proposition>() {
            @Override
            public byte[] closedBoxId() {
                return tokenBox.id();
            }

            @Override
            public Proof boxKey() {
                return tokenBoxOwnershipProof;
            }
        };
        // Append with the CarBox unlocker entry.
        unlockers.add(unlocker);

        return unlockers;
    }

    // Override newBoxes to contains regularBoxes from the parent class appended with MTOBox.
    @Override
    public List<NoncedBox<Proposition>> newBoxes() {
        if(newBoxes == null) {
            newBoxes = new ArrayList<>(super.newBoxes());
            MTOBoxData newTokenBoxData = new MTOBoxData(tokenBox.proposition(), tokenBox.getId(), tokenBox.getBalance()+amount);
            long nonce = getNewBoxNonce(tokenBox.proposition(), newBoxes.size());
            newBoxes.add((NoncedBox) new MTOBox(newTokenBoxData, nonce));
        }
        return Collections.unmodifiableList(newBoxes);
    }
    // Define object serialization, that should serialize both parent class entries and CarBuyOrderInfo as well
    @Override
    public byte[] bytes() {
        ByteArrayOutputStream inputsIdsStream = new ByteArrayOutputStream();
        for(byte[] id: inputRegularBoxIds)
            inputsIdsStream.write(id, 0, id.length);

        byte[] inputRegularBoxIdsBytes = inputsIdsStream.toByteArray();

        byte[] inputRegularBoxProofsBytes = regularBoxProofsSerializer.toBytes(inputRegularBoxProofs);

        byte[] outputRegularBoxesDataBytes = regularBoxDataListSerializer.toBytes(outputRegularBoxesData);

        byte[] inputMTOBoxBytes = tokenBox.bytes();

        byte[] inputMTOBoxOwnershipProofBytes = tokenBoxOwnershipProof.bytes();

        byte[] inputOwnerPropositionBytes = ownerProposition.bytes();

        byte[] amountBytes = Longs.toByteArray(amount);

        return Bytes.concat(
                Longs.toByteArray(fee()),                               // 8 bytes
                Longs.toByteArray(timestamp()),                         // 8 bytes
                Ints.toByteArray(inputRegularBoxIdsBytes.length),       // 4 bytes
                inputRegularBoxIdsBytes,                                // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputRegularBoxProofsBytes.length),    // 4 bytes
                inputRegularBoxProofsBytes,                             // depends on previous value (>=4 bytes)
                Ints.toByteArray(outputRegularBoxesDataBytes.length),   // 4 bytes
                outputRegularBoxesDataBytes,                            // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputMTOBoxBytes.length),              // 4 bytes
                inputMTOBoxBytes,                                       // depends on previous value (>=4 bytes)
                Ints.toByteArray(inputMTOBoxOwnershipProofBytes.length),// 4 bytes
                inputMTOBoxOwnershipProofBytes,
                Ints.toByteArray(inputOwnerPropositionBytes.length),    // 4 bytes
                inputOwnerPropositionBytes,
                amountBytes                                             // 8 bytes
        );
    }

    public static EarnTokenTransaction parseBytes(byte[] bytes) {
        int offset = 0;

        long fee = BytesUtils.getLong(bytes, offset);
        offset += 8;

        long timestamp = BytesUtils.getLong(bytes, offset);
        offset += 8;

        int batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        ArrayList<byte[]> inputRegularBoxIds = new ArrayList<>();
        int idLength = NodeViewModifier$.MODULE$.ModifierIdSize();
        while(batchSize > 0) {
            inputRegularBoxIds.add(Arrays.copyOfRange(bytes, offset, offset + idLength));
            offset += idLength;
            batchSize -= idLength;
        }

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<Signature25519> inputRegularBoxProofs = regularBoxProofsSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        List<RegularBoxData> outputRegularBoxesData = regularBoxDataListSerializer.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        MTOBox mtoBox = MTOBox.parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        Signature25519 proof = Signature25519Serializer.getSerializer().parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        batchSize = BytesUtils.getInt(bytes, offset);
        offset += 4;

        PublicKey25519Proposition proposition = PublicKey25519PropositionSerializer.getSerializer()
                .parseBytes(Arrays.copyOfRange(bytes, offset, offset + batchSize));
        offset += batchSize;

        long amount = BytesUtils.getLong(bytes, offset);

        return new EarnTokenTransaction(inputRegularBoxIds, inputRegularBoxProofs, outputRegularBoxesData,
                fee, timestamp, mtoBox, proof, proposition, amount);
    }

    @Override
    public TransactionSerializer serializer() {
        return EarnTokenTransactionSerializer.getSerializer();
    }
}
