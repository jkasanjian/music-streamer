package rpc; /**
* The rpc.Proxy implements rpc.ProxyInterface class. The class is incomplete
* 
* @author  Oscar Morales-Ponce
* @version 0.15
* @since   2019-01-24 
*/

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Arrays;


public class Proxy implements ProxyInterface {

    private CommunicationModule communicationModule;

    private static Proxy proxy = null;

    public void init(CommunicationModule communicationModule) {
        this.communicationModule = communicationModule;
        CatalogServices.init();
    }

    public static Proxy GetInstance() {
        if (proxy == null) {
            proxy = new Proxy();
        }
        return proxy;
    }

    private Proxy() {
        communicationModule = null;
    }

    private Proxy(CommunicationModule communicationModule)
    {
        this.communicationModule = communicationModule;
        CatalogServices.init();
    }

    public JsonObject synchExecution(String remoteMethod, String[] param) throws Exception {
        JsonObject jsonRequest = CatalogServices.getRemoteReference(remoteMethod, param);
        if(jsonRequest == null) {
            throw new Exception("Remote Method: " + remoteMethod + ", params: " + Arrays.toString(param) + " not found\n");
        }
        String strRet =  this.communicationModule.syncSend(jsonRequest.toString());
        JsonParser parser = new JsonParser();
        return parser.parse(strRet).getAsJsonObject();
    }

    public void asynchExecution(String remoteMethod, String[] param) throws Exception
    {
        JsonObject jsonRequest = CatalogServices.getRemoteReference(remoteMethod, param);
        if(jsonRequest == null) {
            throw new Exception("Remote Method: " + remoteMethod + ", params: " + param.toString() + " not found\n");
        }
        this.communicationModule.asyncSend(jsonRequest.toString());
    }
}

