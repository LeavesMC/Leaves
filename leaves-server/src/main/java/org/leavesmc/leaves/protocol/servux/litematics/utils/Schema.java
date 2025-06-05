package org.leavesmc.leaves.protocol.servux.litematics.utils;

import javax.annotation.Nullable;

public enum Schema {
    // TODO --> Add Schema Versions to this as versions get released
    // Minecraft Data Versions
    SCHEMA_FUTURE(9999, "FUTURE"),
    SCHEMA_1_21_05(4325, "1.21.5"),
    SCHEMA_25W10A(4319, "25w10a"),
    SCHEMA_25W03A(4304, "25w03a"), // Entity Data Components ( https://www.minecraft.net/en-us/article/minecraft-snapshot-25w03a )
    SCHEMA_25W02A(4298, "25w02a"),
    SCHEMA_1_21_04(4189, "1.21.4"),
    SCHEMA_24W46A(4178, "24w46a"),
    SCHEMA_24W44A(4174, "24w44a"),
    SCHEMA_1_21_03(4082, "1.21.3"),
    SCHEMA_1_21_02(4080, "1.21.2"),
    SCHEMA_24W40A(4072, "24w40a"),
    SCHEMA_24W37A(4065, "24w37a"),
    SCHEMA_24W35A(4062, "24w35a"),
    SCHEMA_24W33A(4058, "24w33a"),
    SCHEMA_1_21_01(3955, "1.21.1"),
    SCHEMA_1_21_00(3953, "1.21"),
    SCHEMA_24W21A(3946, "24w21a"),
    SCHEMA_24W18A(3940, "24w18a"),
    SCHEMA_1_20_05(3837, "1.20.5"),
    SCHEMA_24W14A(3827, "24w14a"),
    SCHEMA_24W13A(3826, "24w13a"),
    SCHEMA_24W12A(3824, "24w12a"),
    SCHEMA_24W10A(3821, "24w10a"),
    SCHEMA_24W09A(3819, "24w09a"), // Data Components ( https://minecraft.wiki/w/Data_component_format )
    SCHEMA_24W07A(3817, "24w07a"),
    SCHEMA_24W03A(3804, "24w03a"),
    SCHEMA_23W51A(3801, "23w51a"),
    SCHEMA_1_20_04(3700, "1.20.4"),
    SCHEMA_23W46A(3691, "23w46a"),
    SCHEMA_23W43B(3687, "23w43b"),
    SCHEMA_23W40A(3679, "23w40a"),
    SCHEMA_1_20_02(3578, "1.20.2"),
    SCHEMA_23W35A(3571, "23w35a"),
    SCHEMA_23W31A(3567, "23w31a"),
    SCHEMA_1_20_01(3465, "1.20.1"),
    SCHEMA_1_20_00(3463, "1.20"),
    SCHEMA_23W18A(3453, "23w18a"),
    SCHEMA_23W16A(3449, "23w16a"),
    SCHEMA_23W12A(3442, "23w12a"),
    SCHEMA_1_19_04(3337, "1.19.4"),
    SCHEMA_1_19_03(3218, "1.19.3"),
    SCHEMA_1_19_02(3120, "1.19.2"),
    SCHEMA_1_19_01(3117, "1.19.1"),
    SCHEMA_1_19_00(3105, "1.19"),
    SCHEMA_22W19A(3096, "22w19a"),
    SCHEMA_22W16A(3091, "22w16a"),
    SCHEMA_22W11A(3080, "22w11a"),
    SCHEMA_1_18_02(2975, "1.18.2"),
    SCHEMA_1_18_01(2865, "1.18.1"),
    SCHEMA_1_18_00(2860, "1.18"),
    SCHEMA_21W44A(2845, "21w44a"),
    SCHEMA_21W41A(2839, "21w41a"),
    SCHEMA_21W37A(2834, "21w37a"),
    SCHEMA_1_17_01(2730, "1.17.1"),
    SCHEMA_1_17_00(2724, "1.17"),
    SCHEMA_21W20A(2715, "21w20a"),
    SCHEMA_21W15A(2709, "21w15a"),
    SCHEMA_21W10A(2699, "21w10a"),
    SCHEMA_21W05A(2690, "21w05a"),
    SCHEMA_20W49A(2685, "20w49a"),
    SCHEMA_20W45A(2681, "20w45a"),
    SCHEMA_1_16_05(2586, "1.16.5"),
    SCHEMA_1_16_04(2584, "1.16.4"),
    SCHEMA_1_16_03(2580, "1.16.3"),
    SCHEMA_1_16_02(2578, "1.16.2"),
    SCHEMA_1_16_01(2567, "1.16.1"),
    SCHEMA_1_16_00(2566, "1.16"),
    SCHEMA_20W22A(2555, "20w22a"),
    SCHEMA_20W15A(2525, "20w15a"),
    SCHEMA_20W06A(2504, "20w06a"),
    SCHEMA_1_15_02(2230, "1.15.2"),
    SCHEMA_1_15_01(2227, "1.15.1"),
    SCHEMA_1_15_00(2225, "1.15"),
    SCHEMA_19W46B(2217, "19w46b"),
    SCHEMA_19W40A(2208, "19w40a"),
    SCHEMA_19W34A(2200, "19w34a"),
    SCHEMA_1_14_04(1976, "1.14.4"),
    SCHEMA_1_14_03(1968, "1.14.3"),
    SCHEMA_1_14_02(1963, "1.14.2"),
    SCHEMA_1_14_01(1957, "1.14.1"),
    SCHEMA_1_14_00(1952, "1.14"),
    SCHEMA_19W14B(1945, "19w14b"),
    SCHEMA_19W08B(1934, "19w08b"),
    SCHEMA_18W50A(1919, "18w50a"),
    SCHEMA_18W43A(1901, "18w43a"),
    SCHEMA_1_13_02(1631, "1.13.2"),
    SCHEMA_1_13_01(1628, "1.13.1"),
    SCHEMA_1_13_00(1519, "1.13"),
    SCHEMA_18W22C(1499, "18w22c"),
    SCHEMA_18W14B(1481, "18w14b"),
    SCHEMA_18W07C(1469, "18w07c"),
    SCHEMA_17W50A(1457, "17w50a"),
    SCHEMA_17W47A(1451, "17w47a"), // The Flattening ( https://minecraft.wiki/w/Java_Edition_1.13/Flattening )
    SCHEMA_17W46A(1449, "17w46a"),
    SCHEMA_17W43A(1444, "17w43a"),
    SCHEMA_1_12_02(1343, "1.12.2"),
    SCHEMA_1_12_01(1241, "1.12.1"),
    SCHEMA_1_12_00(1139, "1.12"),
    SCHEMA_1_11_02(922, "1.11.2"),
    SCHEMA_1_11_00(819, "1.11"),
    SCHEMA_1_10_02(512, "1.10.2"),
    SCHEMA_1_10_00(510, "1.10"),
    SCHEMA_1_09_04(184, "1.9.4"),
    SCHEMA_1_09_00(169, "1.9"),
    SCHEMA_15W32A(100, "15w32a");

    private final int schemaId;
    private final String str;

    Schema(int id, String ver) {
        this.schemaId = id;
        this.str = ver;
    }

    /**
     * Returns the Schema of the closest dataVersion, or below it.
     *
     * @param dataVersion (Schema ID)
     * @return (Schema | null)
     */
    public static @Nullable Schema getSchemaByDataVersion(int dataVersion) {
        for (Schema schema : Schema.values()) {
            if (schema.getDataVersion() <= dataVersion) {
                return schema;
            }
        }

        return null;
    }

    public int getDataVersion() {
        return this.schemaId;
    }

    public String getString() {
        return this.str;
    }

    @Override
    public String toString() {
        return "MC: " + this.getString() + " [Schema: " + this.getDataVersion() + "]";
    }
}

