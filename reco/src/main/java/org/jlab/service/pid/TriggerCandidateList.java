/*                                                                                                                                                             
 * To change this license header, choose License Headers in Project Properties.                                                                                
 * To change this template file, choose Tools | Templates                                                                                                      
 * and open the template in the editor.                                                                                                                        
 */
package org.jlab.service.pid;

import java.util.HashMap;



/**                                                                                                                                                            
 *                                                                                                                                                             
 * @author jnewton                                                                                                                                           
 */
public interface TriggerCandidateList {
   
    HashMap<Integer,DetectorParticle>  getCandidates(DetectorEvent event);
}
