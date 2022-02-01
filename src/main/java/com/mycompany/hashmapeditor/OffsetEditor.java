/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.HashmapEditor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OffsetEditor {

    public static void main(String[] args) {
        CommandLineParser clp = new CommandLineParser(args);
        String[] offset_path = clp.getArgumentValue("offset_path");
        String[] commit_scn = clp.getArgumentValue("commit_scn");
        String[] transaction_id = clp.getArgumentValue("transaction_id");
        String[] scn = clp.getArgumentValue("scn");
        String[] snapshot_true = clp.getArgumentValue("snapshot_true");
        String[] snapshot_completed = clp.getArgumentValue("snapshot_completed");

        Map<ByteBuffer, ByteBuffer> data = loadAndReplace(offset_path, commit_scn, transaction_id, scn, snapshot_true, snapshot_completed);
        save(offset_path,data);

    }

    private static Map<ByteBuffer, ByteBuffer> loadAndReplace(String offsetFilePath[], String commit_scn[], String transaction_id[], String scn[],
            String snapshot_true[], String snapshot_completed[]) {

        Map<ByteBuffer, ByteBuffer> data = new HashMap<>();
        String originalValue = "";

        try {

            FileInputStream fileIn = new FileInputStream(offsetFilePath[0]);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            Object obj = objectIn.readObject();

            System.out.println("The Object has been read from the file");

            if (!(obj instanceof HashMap)) {
                throw new ConnectException("Expected HashMap but found " + obj.getClass());
            }

            Map<byte[], byte[]> raw = (Map<byte[], byte[]>) obj;

            for (Map.Entry<byte[], byte[]> mapEntry : raw.entrySet()) {
                ByteBuffer key = (mapEntry.getKey() != null) ? ByteBuffer.wrap(mapEntry.getKey()) : null;
                ByteBuffer value = (mapEntry.getValue() != null) ? ByteBuffer.wrap(mapEntry.getValue()) : null;

                //all the info we want to edit is sitting the value part of the key-value pair
                originalValue = new String(value.array(), "UTF-8");
                String adjustedValue[] = originalValue
                        .replace("{", "")
                        .replace("}", "")
                        .split(",");
                String new_entry = "";

                for (String a : adjustedValue) {
                    if (a.contains("\"commit_scn\"")) {
                        if (commit_scn != null && commit_scn.length > 0) {
                            new_entry = createNewString(a, commit_scn[0]);
                            originalValue = originalValue.replace(a, new_entry);
                        }
                    }

                    if (a.contains("\"transaction_id\"")) {
                        if (transaction_id != null && transaction_id.length > 0) {
                            new_entry = createNewString(a, transaction_id[0]);
                            originalValue = originalValue.replace(a, new_entry);
                        }
                    }

                    if (a.contains("\"scn\"")) {
                        if (scn != null && scn.length > 0) {
                            new_entry = createNewString(a, scn[0]);
                            originalValue = originalValue.replace(a, new_entry);
                        }
                    }
                    if (a.contains("\"snapshot_true\"")) {
                        if (snapshot_true != null && snapshot_true.length > 0) {
                            new_entry = createNewString(a, snapshot_true[0]);
                            originalValue = originalValue.replace(a, new_entry);
                        }
                    }

                    if (a.contains("\"snapshot_completed\"")) {
                        if (snapshot_completed != null && snapshot_completed.length > 0) {
                            new_entry = createNewString(a, snapshot_completed[0]);
                            originalValue = originalValue.replace(a, new_entry);
                        }
                    }
                }

                ByteBuffer revisedByteArray = ByteBuffer.wrap(originalValue.getBytes(StandardCharsets.US_ASCII));
                data.put(key, revisedByteArray);
            }
            System.out.println("Closing object");
            objectIn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return data;

    }

    private static void save(String offsetFilePath[], Map<ByteBuffer, ByteBuffer> data) {
        try {

            FileOutputStream fileOut = new FileOutputStream(offsetFilePath[0]);
            Map<byte[], byte[]> raw = new HashMap<>();
            for (Map.Entry<ByteBuffer, ByteBuffer> mapEntry : data.entrySet()) {
                byte[] key = (mapEntry.getKey() != null) ? mapEntry.getKey().array() : null;
                byte[] value = (mapEntry.getValue() != null) ? mapEntry.getValue().array() : null;
                raw.put(key, value);
            }
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(raw);
            System.out.println("The Object has been written to the file");
            System.out.println("Closing object");
            objectOut.close();
            
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String createNewString(String oldString, String newString) {
        return oldString.split(":")[0] + ":" + "\"" + newString + "\"";
    }
}
