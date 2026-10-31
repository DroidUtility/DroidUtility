package com.frostre1997.droidutility.service;

import android.os.RemoteException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShizukuUserService extends ICommandService.Stub {
    @Override
    public String execute(String command) throws RemoteException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
