package rpc; /**
* The rpc.Dispatcher implements rpc.DispatcherInterface.
*
* @author  Oscar Morales-Ponce
* @version 0.15
* @since   02-11-2019 
*/

import java.util.HashMap;
import java.util.*;
import java.lang.reflect.*;

import com.google.gson.*;
import model.ProfileAccount;


public class Dispatcher extends Thread implements DispatcherInterface {
    public HashMap<String, Object> ListOfObjects;
    public HashMap<String, HashMap<String, String>> atMostOnce;
    
    public Dispatcher()
    {
        ListOfObjects = new HashMap<String, Object>();
        atMostOnce    = new HashMap<>();
    }
    
    /* 
    * dispatch: Executes the remote method in the corresponding Object
    * @param request: Request: it is a Json file
    {
        "remoteMethod":"getSongChunk",
        "objectName":"SongServices",
        "param":
          {
              "song":490183,
              "fragment":2
          }
    }
    */
    public String dispatch(String request)
    {
        System.out.println("IN DISPATCHER" + request);
        JsonObject jsonReturn = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonObject jsonRequest = parser.parse(request).getAsJsonObject();

        try {
            if( jsonRequest.get("call_semantics").equals("At-most-once")){
                if( (atMostOnce.containsKey( jsonRequest.get("username")) || (atMostOnce.containsKey( jsonRequest.get("sessionID"))) )){
                    if( atMostOnce.get( jsonRequest.get("username") )
                            .containsKey(jsonRequest.toString())){
                        return atMostOnce.get( jsonRequest.get("username"))
                                .get(jsonRequest.toString());
                    } else if(atMostOnce.get(jsonRequest.get("sessionID")).containsKey(jsonRequest.toString())){
                        return atMostOnce.get(jsonRequest.get("sessionID")).get(jsonRequest.toString());
                    }
                }
            }

            // Obtains the object pointing to SongServices
            Object object = ListOfObjects.get(jsonRequest.get("objectName").getAsString());
            Method[] methods = object.getClass().getMethods();
            Method method = null;
            // Obtains the method
            for (int i=0; i<methods.length; i++)
            {   
                if (methods[i].getName().equals(jsonRequest.get("remoteMethod").getAsString()))
                    method = methods[i];
            }
            if (method == null)
            {
                jsonReturn.addProperty("error", "Method does not exist");
                return jsonReturn.toString();
            }
            // Prepare the  parameters 
            Class[] types =  method.getParameterTypes();
            Object[] parameter = new Object[types.length];
            String[] strParam = new String[types.length];
            JsonObject jsonParam = jsonRequest.get("param").getAsJsonObject();
            int j = 0;
            for (Map.Entry<String, JsonElement>  entry  :  jsonParam.entrySet())
            {
                strParam[j++] = entry.getValue().getAsString();
            }
            // Prepare parameters
            for (int i=0; i<types.length; i++)
            {
                switch (types[i].getCanonicalName())
                {
                    case "java.lang.Long":
                        parameter[i] =  Long.parseLong(strParam[i]);
                        break;
                    case "int":
                        parameter[i] =  Integer.parseInt(strParam[i]);
                        break;
                    case "java.lang.Integer":
                        parameter[i] =  Integer.parseInt(strParam[i]);
                        break;
                    case "java.lang.String":
                        parameter[i] = new String(strParam[i]);
                        break;
                }
            }
            // Prepare the return
            Class returnType = method.getReturnType();
            String ret = "";
            switch (returnType.getCanonicalName())
                {
                    case "java.lang.Long":
                        ret = method.invoke(object, parameter).toString();
                        break;
                    case "int":
                        ret = method.invoke(object, parameter).toString();
                        break;
                    case "java.lang.String":
                        ret = (String)method.invoke(object, parameter);
                        break;
                }

            Gson gson = new GsonBuilder().create();

            JsonObject obj;
            try {
                obj = parser.parse(ret).getAsJsonObject();
                jsonReturn.add("ret", obj);
            } catch (Exception e) {
                jsonReturn.add("ret", parser.parse(gson.toJson(new ProfileAccount(ret,""))));
            }


            JsonObject goalParam = jsonRequest.get("param").getAsJsonObject();
            String paramT ="";
            for (Map.Entry<String, JsonElement>  entry  :  goalParam.entrySet())
            {
                paramT = entry.getValue().getAsString();
                break;
            }


            if( jsonRequest.get("call_semantics").equals("At-most-once")){




                if( atMostOnce.containsKey( jsonRequest.get("username") )|| atMostOnce.containsKey( jsonRequest.get("sessionID"))) {

                    if( atMostOnce.containsKey( jsonRequest.get("username") ) ) {
                        atMostOnce.get(jsonRequest.get("username")).put( jsonRequest.toString(), jsonReturn.toString() );
                    }
                    else{
                        atMostOnce.get(jsonRequest.get("sessionID")).put( jsonRequest.toString(), jsonReturn.toString() );
                    }
                }
                else{

                    if(paramT.equals("username")){
                        atMostOnce.put(jsonRequest.get("username").toString(), new HashMap<>());
                        atMostOnce.get(jsonRequest.get("username")).put( jsonRequest.toString(), jsonReturn.toString() );
                    } else {
                        atMostOnce.put(jsonRequest.get("sessionID").toString(), new HashMap<>());
                        atMostOnce.get(jsonRequest.get("sessionID")).put( jsonRequest.toString(), jsonReturn.toString() );
                    }
                }


            }
        } catch (InvocationTargetException | IllegalAccessException e)
        {
        //    System.out.println(e);
            jsonReturn.addProperty("error", "Error on " + jsonRequest.get("objectName").getAsString() + "." + jsonRequest.get("remoteMethod").getAsString());
            e.printStackTrace();
        }
     
        return jsonReturn.toString();
    }

    public void run(){

    }

    /* 
    * registerObject: It register the objects that handle the request
    * @param remoteMethod: It is the name of the method that 
    *  objectName implements. 
    * @objectName: It is the main class that contaions the remote methods
    * each object can contain several remote methods
    */
    public void registerObject(Object remoteMethod, String objectName)
    {
        ListOfObjects.put(objectName, remoteMethod);
    }
    
    /*  Testing
    public static void main(String[] args) {
        // Instance of the rpc.Dispatcher
        rpc.Dispatcher dispatcher = new rpc.Dispatcher();
        // Instance of the services that te dispatcher can handle
        rpc.SongDispatcher songDispatcher = new rpc.SongDispatcher();
        
        dispatcher.registerObject(songDispatcher, "SongServices");  
    
        // Testing  the dispatcher function
        // First we read the request. In the final implementation the jsonRequest
        // is obtained from the communication module
        try {
            String jsonRequest = new String(Files.readAllBytes(Paths.get("./getSongChunk.json")));
            String ret = dispatcher.dispatch(jsonRequest);
            System.out.println(ret);

            //System.out.println(jsonRequest);
        } catch (Exception e)
        {
            System.out.println(e);
        }
        
    }*/
}
