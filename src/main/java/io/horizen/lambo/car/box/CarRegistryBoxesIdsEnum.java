package io.horizen.lambo.car.box;

// Declare all custom box type ids in a single enum to avoid collisions.
// Used during Boxes serializations.
public enum CarRegistryBoxesIdsEnum {
    CarBoxId((byte)1),
    CarSellOrderBoxId((byte)2),
    MTOBoxId((byte)3);

    private final byte id;

    CarRegistryBoxesIdsEnum(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }
}
