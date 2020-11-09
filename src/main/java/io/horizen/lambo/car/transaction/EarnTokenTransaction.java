package io.horizen.lambo.car.transaction;

import com.horizen.box.BoxUnlocker;
import com.horizen.box.NoncedBox;
import com.horizen.box.data.RegularBoxData;
import com.horizen.proof.Proof;
import com.horizen.proof.Signature25519;
import com.horizen.proposition.Proposition;
import com.horizen.proposition.PublicKey25519Proposition;
import com.horizen.transaction.TransactionSerializer;
import io.horizen.lambo.car.box.CarSellOrderBox;
import io.horizen.lambo.car.box.MTOBox;
import io.horizen.lambo.car.box.data.MTOBoxData;

import java.util.ArrayList;
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

    @Override
    public TransactionSerializer serializer() {
        return null;
    }
}
