package mrs.crypto;

import it.unisa.dia.gas.jpbc.Element;

import java.util.ArrayList;

// NEW
public class FAMECipherText {
    String policy_str;
    ArrayList<Element> C_0;

    // Attribute label corresponding to each MSP row.
    ArrayList<String> C_attributes;

    // Ciphertext components stored separately for every MSP row.
    ArrayList<ArrayList<Element>> C;

    Element Cp;
    byte[] aesBuf;
}