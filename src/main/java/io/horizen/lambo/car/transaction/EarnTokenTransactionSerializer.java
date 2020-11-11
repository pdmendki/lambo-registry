package io.horizen.lambo.car.transaction;

import com.horizen.transaction.TransactionSerializer;
import scorex.util.serialization.Reader;
import scorex.util.serialization.Writer;

public class EarnTokenTransactionSerializer implements TransactionSerializer<EarnTokenTransaction>  {

    private static EarnTokenTransactionSerializer serializer = new EarnTokenTransactionSerializer();

    private EarnTokenTransactionSerializer() {
        super();
    }

    public static EarnTokenTransactionSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(EarnTokenTransaction transaction, Writer writer) {
        writer.putBytes(transaction.bytes());
    }

    @Override
    public EarnTokenTransaction parse(Reader reader) {
        return EarnTokenTransaction.parseBytes(reader.getBytes(reader.remaining()));
    }
}
