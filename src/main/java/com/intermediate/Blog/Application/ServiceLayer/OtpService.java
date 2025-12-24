package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.DtoLayers.PendingUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    private Map<String , PendingUser> pendingRegistrations = new HashMap<>();

    public void savePendingUser(String email , PendingUser user){
        pendingRegistrations.put(email , user);
    }

    public PendingUser getPendingUser(String email){
        return pendingRegistrations.get(email);
    }

    public void removePendingUser(String email){
        pendingRegistrations.remove(email);
    }

}
