package server.endpoints;


import com.google.gson.Gson;
import server.controller.MainController;
import server.controller.TokenController;
import server.models.User;
import server.utility.Crypter;
import server.utility.CurrentUserContext;
import server.utility.Globals;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/user")
public class UserEndpoint {
    MainController mainController = new MainController();
    TokenController tokenController = new TokenController();

    Crypter crypter = new Crypter();

    @POST
    @Path("/login")
    //Endpoint for authorizing a user
    public Response logIn(String user) {
        //User authorizedUser = mainController.authUser(new Gson().fromJson(user, User.class));
        String newUser = crypter.encryptAndDecryptXor(user);

        String authorizedUser = mainController.authUser(new Gson().fromJson(newUser, User.class));

        authorizedUser = crypter.encryptAndDecryptXor(authorizedUser);

        if (authorizedUser != null) {
            return Response.status(200).type("application/json").entity(new Gson().toJson(authorizedUser)).build();
        } else {
            return Response.status(401).type("text/plain").entity("Error signing in - unauthorized").build();
        }
    }

    @POST
    @Path("/signup")
    //Creating a new user
    public Response signUp(String user) {
        String newUser = crypter.encryptAndDecryptXor(user);
        User createdUser = mainController.createUser(new Gson().fromJson(newUser, User.class));
        String newestUser = new Gson().toJson(createdUser);
        newestUser = crypter.encryptAndDecryptXor(newestUser);

        if (createdUser != null) {
            return Response.status(200).type("application/json").entity(new Gson().toJson(newestUser)).build();
        } else {
            return Response.status(400).type("text/plain").entity("Error creating user").build();
        }
    }


    @Path("/myuser")
    @GET
    //Getting own profile by token
    public Response getMyUser(@HeaderParam("authorization") String token) throws SQLException {
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);
        String myUser = new Gson().toJson(currentUser.getCurrentUser());
        myUser = crypter.encryptAndDecryptXor(myUser);

        if (currentUser.getCurrentUser() != null) {
            return Response.status(200).type("application/json").entity(new Gson().toJson(myUser)).build();
        } else {
            return Response.status(400).type("text/plain").entity("Error loading user").build();
        }
    }

    @POST
    @Path("/logout")
    public Response logOut(String userId) throws SQLException {
        int myUserId = new Gson().fromJson(userId, Integer.class);
        Boolean deletedToken = tokenController.deleteToken(myUserId);

        if (deletedToken == true) {
            return Response.status(200).entity("Logged out").build();
        } else {
            return Response.status(400).type("text/plain").entity("Error logging out").build();
        }


    }
}