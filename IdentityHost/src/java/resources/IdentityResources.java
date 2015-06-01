/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import datasource.IdentityDS;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service.
 */
@Stateless
@Path("/identity")
public class IdentityResources {
    /**
     * Metodo para verificar se o servidor está funcionando.
     */
    @GET
    @Path("/ping")
    @Produces("application/json")
    public String pingServer() {
        return "ALIVE";
    }
    
    /**
     * Metodo para consultar qual é o lider.
     */
    @GET
    @Path("/getleader")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLeader() {
        return IdentityDS.getLider();
    }
    
    /**
     * Metodo para informar quem é o líder.
     */
    @POST
    @Path("/setleader/{leaderip}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setLeader(@PathParam("leaderip") String ip) {
        IdentityDS.setLider(ip);
        return Response.ok().build();
    }
}
