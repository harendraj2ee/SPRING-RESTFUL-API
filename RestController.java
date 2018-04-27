package com.hitech.controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.net.InetAddresses;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.SimpleQueryParser.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.gson.JsonArray;
import com.hitech.rest.impl.MongoLinkedin;
import com.hitech.rest.impl.RestService;
import com.model.config.CanditateData;
import com.model.config.RootConfig;
import com.sun.syndication.feed.atom.Person;

@Controller

public class RestController {

@Autowired
private RootConfig rootconfig;

@Autowired
private RestTemplate restTemplate;	

@Autowired
private RestService restService;   

@Autowired
MongoLinkedin linkedin_html;

@RequestMapping(value ="/job")
public @ResponseBody String jobData(@RequestParam("qany")String qany,@RequestParam("qall")String qall, @RequestParam("exclude")String exclude,
		@RequestParam("indusgroup")String indusgroup,@RequestParam("industyp")String industyp,@RequestParam("ctc")String ctc,
		@RequestParam("exp")String exp,@RequestParam("grdc")String grdc,@RequestParam("skill")String skill) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException{
       
	//qany=&qall=&exclude=&indusgroup=&industyp=&ctc=&exp=&grdc=&skill=
	
	 String dynamiCQuery="";
	
	 if(dochek4Null(qany)){
         //qany= getAndwithData(qany,"html_data  OR EmailID OR Mobile","OR","");
          String  mailany =URLEncoder.encode(qany, "UTF-8");
          String  regex="[0-9]";
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(qany);
          
     	 if(mailany.contains("%40")){
     		 //System.out.println("jjjjj");
     		 qany= getAndwithData(qany,"html_data  OR EmailID","OR","");
     		 qany=qany.replace("*", ""); 
     	 }else if(matcher.find()){
     		     //System.out.println("qanyllllllllmmmmmllll");
     		     qany= getAndwithData(qany,"html_data OR Mobile","OR","");
     		     qany=qany.replace("*", ""); 
     	 }else{
     		 //System.out.println("qanyllllllllllll"+qany);
     		 qany= getAndwithData(qany,"html_data","OR",""); //org
     		 //System.out.println("hello any key::::"+qany);
     	 }        
          
        if( dochek4Null(dynamiCQuery)){
	             // dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
        	      dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
	             }else{
	            	  dynamiCQuery="("+qany+")";
	             }
          }
	 // only for  allKeywords
     if( dochek4Null(qall)){
    	String  mail =URLEncoder.encode(qall, "UTF-8");
    	String  regex="[0-9]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(qany);
    	
    	if(mail.contains("%40")){
    		 qall= getAndwithData(qall,"html_data  OR EmailID","AND","");
    		 
    		 //System.out.println("hi contains"+qall);
       	     qall=qall.replace("*", ""); 
       	     //System.out.println("hiqall contains"+qall);
    	 } 
    	  else if(matcher.find()){
 		     //System.out.println("qanyllllllllmmmmmllll");
 		     qany= getAndwithData(qany,"html_data OR Mobile","OR","");
 		     qany=qany.replace("*", ""); 
 		     
 	     }else {
    		 qall= getAndwithData(qall,"html_data","AND","");
    		 qall=qall.replace("\"", "\"\"").replace("*", "");
       	     //System.out.println("sorry contains");
    	 }              
    	
      if( dochek4Null(dynamiCQuery)){
            dynamiCQuery=dynamiCQuery + "AND"+ "("+qall+")";  //OR
            }else{
           	 dynamiCQuery="("+qall+")";
            }
     }
   
     // only fOR qexclude.
		if (dochek4Null(exclude)) {
			exclude = getAndwithData(exclude, "", "", "NOT");
            // qexclude= getAndwithData(qexclude,"html_data","","NOT");
			System.out.println("qexclude::::" + exclude);
			if (dochek4Null(dynamiCQuery)) {
				dynamiCQuery = dynamiCQuery + "NOT" + "(" + exclude + ")";
			} else {
				dynamiCQuery = "(" + exclude + ")";
			}
		}  
		
		
		
		// int start = 100 *(next);
		    
	       // String pQuery="(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata;
	      
	       //System.out.println("Parent Query:::::"+pQuery);
	       //String url=rootconfig.getResumeDataLocal()+"/select?q="+dynamiCQuery+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation&start="+start+"&rows=100&omitHeader=false&wt=json&indent=true";
	       String Rooturl=rootconfig.getResumeDataLive()+"/select?q="+dynamiCQuery+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	       System.out.println(Rooturl);
	       
	       URL url = new URL(Rooturl);
	 	   String nullFragment = null;
	       URI UOrig = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),url.getQuery(), nullFragment);
	       
	 		//System.out.println("Original Url:" + UOrig);
	 	String	jsonData = restTemplate.getForObject(UOrig, String.class);
	 
	        return jsonData;
      }


@RequestMapping(value="/resume")
public @ResponseBody String resume(@RequestParam("id") int id) throws Exception {

	List<CanditateData> list = new ArrayList<CanditateData>();
	list=linkedin_html.getResumeData(id);
	JSONObject json = new JSONObject();
	json.put("response", list);
	//System.out.println("json data:::"+json);
	return json.toString();	
}


@RequestMapping(value="/mongoId",method={RequestMethod.GET})
public @ResponseBody String mongoId(@RequestParam("id")int id) throws JSONException{
	
	List<CanditateData> list = new ArrayList<CanditateData>();
	list=linkedin_html.getResumeId(id);
	JSONObject json = new JSONObject();
	json.put("response", list);
	//System.out.println("json data:::"+json);
	return json.toString();	
	
}


     


   public static String getAndwithData(String data,String solrField,String Operator, String Operator4NOT){
	  String dataQuery="";
	   if(" "!=data && data.length()>=1){
		  data=data.replace("#", "%23");
	   
	   
	   if(Operator.equals("AND")){
		   System.out.println("Going to Craete And Operator");
	   	   data = "\"*" + data.replace(",", "*\" AND \"*") + "*\"";
	   }
	   
	   if(Operator.equals("OR")){
		   System.out.println("Going to Craete And Operator");
	   	   data = "\"*" + data.replace(",", "*\" OR \"*") + "*\"";
	   }
	   
	   if(Operator.equals("TO")){
		   System.out.println("Going to convert comma with TO");
		   data=data.replace(",", "TO");
		   data= "[" +data+ "]";
	   }
	   
	   if(Operator.equals("SPACE")){
		   System.out.println("Going to convert space with +");
		   data=data.replace(" ", "+");
		}
	  
	   if(Operator4NOT.equals("NOT")){
		   System.out.println("Going to  use Not for exclude ");            		               
	        data= " NOT (_text_:"+data+")";
	   }
	   
	   }else 
		  data="*"; {
		}
		  
		  
	if(solrField.equals("TotalExps")){
		data = "\"*" + data.replace(",", "*\" OR \"*") + "*\"";
	    data = data.replace("OR", "OR TotalExps:").replaceFirst("\"", "");
	}
	
	 if(solrField.equals("CurrentCTC")){
    	 data = "\"*" + data.replace(",", "*\" OR \"*") + "*\"";
    	 data = data.replace("OR", "OR CurrentCTC:").replaceFirst("\"", "");
     }
	 
	 dataQuery=solrField+":"+data;
   	 //System.out.println("dataQuery:::::::::::::::"+dataQuery);
	  dataQuery="("+dataQuery+")";
     return dataQuery;
}
   
   



@RequestMapping(value ="/naukriData")
public @ResponseBody String can_Service(@RequestParam("canName") String canName,@RequestParam("presentEmp") String presentEmp,
	   @RequestParam("desigText") String desigText,@RequestParam("graduate") String graduate,@RequestParam("postgraduate") String postgraduate,
	   @RequestParam("SkillsText") String SkillsText,@RequestParam("TotalExp") String TotalExp,@RequestParam("PresentCTC") String PresentCTC,
	   @RequestParam("current_work_loc") String current_work_loc){
	   String jsonData="";
	try{
		
		  canName=canName.toLowerCase();
		  canName= "\""+canName+"\"";
		  //canName= '*'+canName+'*';
		  System.out.println("canName::::"+canName);
		  
		  presentEmp=presentEmp.toLowerCase();
		  presentEmp="\""+presentEmp+"\"";
		  
		  desigText=desigText.toLowerCase();
		  desigText="\""+desigText+"\"";
		  
		  graduate=graduate.toLowerCase();
		  graduate="\""+graduate+"\"";
		  
		  postgraduate=postgraduate.toLowerCase();
		  postgraduate="\""+postgraduate+"\"";
		  
		  SkillsText=SkillsText.toLowerCase();
		  
		  
		  TotalExp=TotalExp.toLowerCase();
		  
		  PresentCTC=PresentCTC.toLowerCase();
		 
		  current_work_loc=current_work_loc.toLowerCase();
		  current_work_loc="\""+current_work_loc+"\"";
		  
		  SkillsText = "\"*" + SkillsText.replace(",", "*\" OR \"*") + "*\"";
		  
		  System.out.println("hi local"+rootconfig.getResumeDataLocal());
		  System.out.println("hi live"+rootconfig.getResumeDataLive());
		  
			//String urlForDotnet="http://192.168.1.95:8983/solr/resume_shard1_replica1/select?q=candidate_name "+canName+"&fl=Designationtext,TotalExp,PresentEmployer,candidate_name,PresentCTC,skillstext,current_work_loc,graduate,postgraduate&rows=5&wt=json&indent=true";	
		//imp	 String urlForDotnet="http://192.168.1.95:8983/solr/resume_shard1_replica1/select?q=(candidate_name : "+canName+") AND (SkillsText : "+SkillsText+"  PresentEmployer : "+presentEmp+" DesignationText : "+desigText+" graduate : "+graduate+" postgraduate : "+postgraduate+" PresentCTC : "+PresentCTC+" current_work_loc : "+current_work_loc+" TotalExp : "+TotalExp+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true&rows=5";
			 
			 String urlForDotnet=rootconfig.getResumeDataLive()+"/select?q=(candidate_name : "+canName+") AND (SkillsText : "+SkillsText+"  PresentEmployer : "+presentEmp+" DesignationText : "+desigText+" graduate : "+graduate+" postgraduate : "+postgraduate+" PresentCTC : "+PresentCTC+" current_work_loc : "+current_work_loc+" TotalExp : "+TotalExp+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true&rows=5";

				    //if(urlForDotnet.contains(" ")||urlForDotnet.contains(":")||urlForDotnet.contains(",")) {
					if(urlForDotnet.contains(" ") ||urlForDotnet.contains(",")) {
					//urlForDotnet = urlForDotnet.replace(" ", "+").replace(",", "%2C");
					urlForDotnet = urlForDotnet.replace(" ", "+");
	               // System.out.println("final :::"+urlForDotnet);
	        }
	    /*
		String urlForDotnet="http://192.168.1.95:8983/solr/resume_shard1_replica1/select?fl=Designationtext,TotalExp,PresentEmployer,candidate_name,PresentCTC,skillstext,current_work_loc,graduate,postgraduate&q=candidate_name:"+canName+" AND PresentEmployer:"+presentEmp+" OR SkillsText:"+SkillsText+" OR current_work_loc:"+current_work_loc+"  OR Designationtext:"+desigText+" OR graduate:"+graduate+" OR postgraduate:"+postgraduate+" OR TotalExp:"+totalExp+" OR PresentCTC:"+PresentCTC+" "
				+ "&rows=5&wt=json&indent=true";*/

		/*+"&DesignationText:"+desigText+"&PresentEmployer:"+presentEmp+"&graduate:"+graduate+""
		 +"&postgraduate:"+postgraduate+"&SkillsText:"+SkillsText+"&FunctionText:"+FunctionText+"&PresentCTC:"+presentCtc+"&TotalExp:"+totalExp+"&candidate_name:"+canName+"&current_work_loc:"+curLoc+"&Industrytext:"+industryText+"&*%3A*&wt=json&indent=true";*/
		System.out.println("url For naukriData::"+urlForDotnet);
		 jsonData = restTemplate.getForObject(urlForDotnet, String.class);
		 
	}catch(Exception e){
		e.printStackTrace();
        String str = e.getMessage();
		//System.out.println("stringg:::"+str);
		if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
	}
	
  return jsonData;
}


@RequestMapping(value="/exactData")
public @ResponseBody String exact_Search(@RequestParam("canName") String canName,@RequestParam("presentEmp") String presentEmp ,
	@RequestParam("desigText") String desigText,@RequestParam("graduate") String graduate,@RequestParam("postgraduate") String postgraduate,
	@RequestParam("SkillsText") String SkillsText,@RequestParam("TotalExp") String TotalExp,@RequestParam("PresentCTC") String PresentCTC,
	@RequestParam("current_work_loc") String current_work_loc){
    String jsonFeed="";
    try{
    	  canName=canName.toLowerCase();
		  presentEmp=presentEmp.toLowerCase();
		  desigText=desigText.toLowerCase();
		  
		  graduate=graduate.toLowerCase();
		  postgraduate=postgraduate.toLowerCase();
		  SkillsText=SkillsText.toLowerCase();
		  TotalExp=TotalExp.toLowerCase();
		  PresentCTC=PresentCTC.toLowerCase();
		  current_work_loc=current_work_loc.toLowerCase();
    	  SkillsText = "\"*" + SkillsText.replace(",", "*\" OR \"*") + "*\"";
          
		  //String urlForDotnet="http://192.168.1.95:8983/solr/resume_shard1_replica1/select?q=(candidate_name : +\""+canName+"\"~1) AND (PresentEmployer : +\""+presentEmp+"\"~1)&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate&wt=json&indent=true&rows=5";

		//imp  String urlForDotnet="http://192.168.1.95:8983/solr/resume_shard1_replica1/select?q=candidate_name : +\""+canName+"\"~1&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true&rows=5";
		 // fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate&wt=json&indent=true&rows=5";
		  
		  String urlForDotnet=rootconfig.getResumeDataLive()+"/select?q=candidate_name : +\""+canName+"\"~1&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true&rows=5";

		  		//+ "SkillsText : "+SkillsText+"  PresentEmployer : "+presentEmp+" DesignationText : "+desigText+" graduate : "+graduate+" postgraduate : "+postgraduate+" PresentCTC : "+PresentCTC+" current_work_loc : "+current_work_loc+" TotalExp : "+TotalExp+"&fl=DesignationText,TotalExp,PresentEmployer,candidate_name,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate&wt=json&indent=true&rows=5";
				if(urlForDotnet.contains(" ") ||urlForDotnet.contains(",")) {
				urlForDotnet = urlForDotnet.replace(" ", "+");
                //System.out.println("final Query :::"+urlForDotnet);
				
         }
			
	 System.out.println("url for exactData::"+urlForDotnet);
     jsonFeed = restTemplate.getForObject(urlForDotnet, String.class);
	 //System.out.println("json output:::::::"+jsonFeed);
     }catch(Exception e){
    	e.printStackTrace();
    	String str= e.getMessage();
    	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
    }
  return jsonFeed;
}

/*Very Importent commented by amar*/

/*=================== 24/07/2017================*/

@RequestMapping(value="/htmlDataCmpny")
public @ResponseBody String htmlData(@RequestParam("qname") String qname,@RequestParam("qedu") String qedu,@RequestParam("qeduyr") String qeduyr,@RequestParam("quninver") String quninver, @RequestParam("company") String company){
       String jsonData="";
       try{
       String title="<title>";
       String url="";
       qname= "\""+title+qname+"\"";
    
       qeduyr = "\"*" + qeduyr.replace(",", "*\" AND \"*") + "*\""; 
       
       quninver=quninver.toLowerCase();// change made on 7th Aug from vivek
       quninver = "\"*" + quninver.replace(",", "*\" AND \"*") + "*\"";
       qedu = "\"*" + qedu.replace(",", "*\" AND \"*") + "*\"";
       
       company=company.toLowerCase();// change made on 7th Aug from vivek
       company = "\"*" + company.replace(",", "*\" AND \"*") + "*\"";
       
       System.out.println("company name:::"+company);
       
       //7-15-17
       if(qname.contains(" ")){
    	   System.out.println("with space!!!");
              //System.out.println("with space!!!");  // hi sir here with name with OR but i change OR with AND.
              // url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
              // change on 9th Aug 2017 to get name from Html
              url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
       }else{
    	   System.out.println("without space");
              //System.out.println("without space!!!");
              //url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
              // change on 9th Aug 2017 to get name from Html
              url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
    }
       
       if(url.contains(" ") ||url.contains(",")) {
              url = url.replace(" ", "+");
              //System.out.println("url>>>>"+url);
       }      
       
       System.out.println("url for htmlData With Company::: "+url);
       jsonData=restTemplate.getForObject(url, String.class);
       }catch(Exception e){
       e.printStackTrace();
       String str= e.getMessage();
       if (str.contains("400 Bad Request")) {
                     return "{\"error\":\"data should not be blank\"}";
              }
       }
       return jsonData;
      
}
       
       /*==================== close===================*/


@RequestMapping(value="/htmlData")
public @ResponseBody String htmlData(@RequestParam("qname") String qname,@RequestParam("qedu") String qedu,@RequestParam("qeduyr") String qeduyr,@RequestParam("quninver") String quninver){
       String jsonData="";
       try{
    	  
       String title ="<title>";
       String url="";
       qname= "\""+title+qname+"\"";
   
       qeduyr = "\"*" + qeduyr.replace(",", "*\" AND \"*") + "*\""; 
       
       quninver=quninver.toLowerCase();// change made on 7th Aug from vivek
       quninver = "\"*" + quninver.replace(",", "*\" AND \"*") + "*\"";
        
       
       qedu = "\"*" + qedu.replace(",", "*\" AND \"*") + "*\"";
       //System.out.println("qname:::"+qname);
       
       // imp String url="http://192.168.1.237:8983/solr/resume1_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";
       
       //7777777String url=rootconfig.getResumeDataLocal()+"/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";

       // code comment by vivek 7th Aug
       /*//7-15-17
       if(qname.contains(" ")){
              
              
              //System.out.println("with space!!!");/ // hi sir here with name with OR but i change OR with AND.
              url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
  }else{
              //System.out.println("without space!!!");
         
               url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
   }*/
  //7th Aug 2017 vivek
          if(qname.contains(" ")){                 
               		//System.out.println("with space!!!");/ // hi sir here with name with OR but i change OR with AND.
               		//url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
               		// change on 9th Aug 2017 to get name from Html
                    url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
              }else{
                     //System.out.println("without space!!!");       
                     //url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
                     // change on 9th Aug 2017 to get name from Html
                     url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&hl.fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&hl=true&hl.q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&hl.snippets=10&omitHeader=false&indent=true";
                     
                     
                    // ch1=  url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&hl.fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&hl=true&hl.q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&hl.snippets=10&omitHeader=false&indent=true";
/*                     url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,CurrentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&hl=true&hl.q=(html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&hl.fl=content&hl.snippets=10&omitHeader=false&indent=true";
*/
              }
          
             //String url="http://192.168.1.237:8983/solr/resume_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,html_data&wt=json&indent=true";
       
       if(url.contains(" ") ||url.contains(",")) {
              url = url.replace(" ", "+");
              //System.out.println("url>>>>"+url);
       }    
       
       System.out.println("url for htmlData without cmpny::: "+url);
       jsonData=restTemplate.getForObject(url, String.class);
       }catch(Exception e){
       e.printStackTrace();
       
       String str= e.getMessage();
       if (str.contains("400 Bad Request")){
                     return "{\"error\":\"data should not be blank\"}";
              }
       }
       return jsonData;
       
}

/********InternalSearch**********/

@RequestMapping(value="/showhtml")
public @ResponseBody String showHtml(@RequestParam("id") String id){
	if(id==null || id.equals("")){
		id="1";
	}
	System.out.println("showhtml canid:::::::"+id);
    String url=rootconfig.getResumeDataLocal()+"/select?hl.q=id:"+id+"&fl=id,html_data&omitHeader=true&wt=json&hl=on&indent=true";
    System.out.println("url for showhtml::::"+url);
	
    String html_data= restTemplate.getForObject(url, String.class);
	return html_data;
	
}


@RequestMapping(value="canSearch")
public @ResponseBody String getInternal(@RequestParam("qany") String qany, @RequestParam("qall") String qall) throws MalformedURLException, URISyntaxException{
	String ss="";
	String s1="";
	String s2="";
	if(qany.length()>=1){
		System.out.println("qany:::::::::::::"+qany);
		qany=qany.toLowerCase().replace("#", "%23");
		qany = "\"*" + qany.replace(",", "*\" OR \"*") + "*\""; 
		System.out.println("qanylllllll:::::::::::"+qany);
		//s1= "("+html_data:"+qany+"+);
		s1="(html_data:"+qany+")";
		ss=s1;
		System.out.println("s1:::::::"+ss);
	}
	
	if(qall.length()>=1){
		System.out.println("qall:::::::::::::"+qall);
		qall=qall.toLowerCase().replace("#", "%23");
		qall = "\"*" + qall.replace(",", "*\" AND \"*") + "*\""; 
		System.out.println("qallll:::::::::::"+qall);
		s2="(html_data:"+qall+")";
		ss=s1+" OR "+s2;
		System.out.println("s2:::::::"+ss);
	}
	
	
	  
	  //String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,html_data&start="+start+"&omitHeader=true&rows=100&wt=json&indent=true";
	  String subUrlEmcod=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qany+") OR (html_data:*)&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,html_data&start=0&omitHeader=true&rows=100&wt=json&indent=true";
	  System.out.println("url::::::"+subUrlEmcod);
      //String json=restTemplate.getForObject(subUrlEmcod, String.class);
      
      URL url = new URL(subUrlEmcod);
		String nullFragment = null;

		URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
				url.getQuery(), nullFragment);

		System.out.println("Original Url:" + U);
		String jsonFeed = restTemplate.getForObject(U, String.class);
      
	  return jsonFeed;
	
}





/*@RequestParam("next") Integer next*/

@RequestMapping(value="/is")
public @ResponseBody String getInternalDatas(@RequestParam("qall") String qall,@RequestParam("qany") String qany,@RequestParam("qbol") String qbol, @RequestParam("qexclude") String qexclude, @RequestParam("qctc") String qctc,
	   @RequestParam("qexp") String qexp, @RequestParam("qloc") String qloc, @RequestParam("qploc") String qploc, @RequestParam("qindtyp") String qindtyp, @RequestParam("qcemp") String qcemp,
	   @RequestParam("qpemp") String qpemp,@RequestParam("qcurdesig") String qcurdesig, @RequestParam("qprdesig") String qprdesig, @RequestParam("qfarea") String qfarea, @RequestParam("highlight") String highlight,
		//@RequestParam("qctc") String qctc,@RequestParam("qloc") String qloc, @RequestParam("qfarea") String qfarea, @RequestParam("qindustry") String qindustry, @RequestParam("qdesig") String qdesig ,
	   @RequestParam("next") Integer next ){
	System.out.println("all query data::"+qall);
    System.out.println("highlight value::::::::::::"+highlight);
	
	
	String jsonData ="";
	String ss="";
	String s1="";
	String s2="";
	JSONObject jsonresult=null;
	try{
	
		String query="";
		String sall="";
		String ssany="";
		String ssbol="";
	if(qany.length()>=1){
		System.out.println("qany:::::::::::::"+qany);
		
		//qany=qany.toLowerCase().replace("#", "%23");
		qany = "\"*" + qany.replace(",", "*\" OR \"*") + "*\""; 
		
		System.out.println("qanylllllll:::::::::::"+qany);
		
	//	qany = "_text_:"+qany;
	}
	
	//if(qall.length()>=1){
		qall=qall.toLowerCase().replace("#", "%23");
		qall = "\"*" + qall.replace(",", "*\" AND \"*") + "*\"";
		//qall = "_text_:"+qall;
		//System.out.println("qall::::"+qall);
	//}
		
	
	//if(qbol.length()>=1){
		qbol=qbol.toLowerCase().replace("#", "%23");
		qbol = "\"*" + qbol.replace(",", "*\" AND \"*") + "*\"";
	//	qbol = "_text_:"+qbol;
		//System.out.println("qbol::::"+qbol);
	//}
	
	if(qcemp.length()>=1){
		qcemp = qcemp.toLowerCase().replace("#", "%23");
		//qcemp= "\""+qcemp+"\"";
		qcemp=qcemp.replace(" ", "+");
		qcemp ="\\"+ "\"" +qcemp + "\\" +"\"";
		//System.out.println("qcemp:::::::::::::::"+qcemp);	
	}else{
		qcemp="*";
	}
	
	if(qpemp.length()>=1){
		qpemp = qpemp.toLowerCase().replace("#", "%23");
		//qcemp= "\""+qcemp+"\"";
		qpemp=qpemp.replace(" ", "+");
		qpemp ="\\"+ "\"" +qpemp + "\\" +"\"";
		//System.out.println("previous employer::::::::"+qpemp);	
	}else{
		qpemp="*";
	}

	
	String ex="";
	String exdata="";
	qexclude=qexclude.toLowerCase().replace("#", "%23");
	if(qexclude.length()>=1){
			qexclude = "\"*" + qexclude.replace(",", "*\" OR \"*") + "*\"";
			ex= " NOT (_text_:"+qexclude+")";
			exdata=ex;
	}
	
	qploc=qloc.toLowerCase().replace("#", "%23");
	qploc = "\"*" + qploc.replace(",", "*\" OR \"*") + "*\""; 
	//System.out.println("qploc::::"+qploc);
	
	
	qloc=qloc.toLowerCase().replace("#", "%23");
	qloc = "\"*" + qloc.replace(",", "*\" OR \"*") + "*\""; 
	//System.out.println("qloc::::"+qloc);
	
	if(qfarea.length()>=1){
		qfarea=qfarea.toLowerCase().replace("#", "%23");
		qfarea=qfarea.replace(" ", "+");
		//System.out.println("qfarea::::::"+qfarea);
		//qfarea = "\"*" + qfarea.replace(",", "*\" OR \"*") + "*\"";
		qfarea ="\\"+ "\"" +qfarea + "\\" +"\"";
	}else{
		qfarea="*";
	}

	qindtyp=qindtyp.toLowerCase().replace("#", "%23");
	
	if(qexp.length()>=1){
		
		//(1~ TO 4~)
		qexp = qexp.replace(",", "~ TO ");
		//qexp = "[" +qexp+ "]";
		qexp ="(" +qexp+ "~)";
		
		//System.out.println("qexp::::::::::::"+qexp);
	}else{
		qexp = "*";
	} 
	
	if(qctc.length()>=1){
		qctc = qctc.replace(",", " TO ");
		qctc = "[" +qctc+ "]";
		//System.out.println("");
	}else{
		qctc = "*";
	}
	
	if(qcurdesig.length()>=1){
		qcurdesig = qcurdesig.toLowerCase().replace("#", "%23");
		//qcemp= "\""+qcemp+"\"";
		qcurdesig=qcurdesig.replace(" ", "+");
		qcurdesig ="\\"+ "\"" +qcurdesig + "\\" +"\"";
	}else{
		qcurdesig="*";
	}
	
	if(qprdesig.length()>=1){
		qprdesig = qprdesig.toLowerCase().replace("#", "%23");
		//qcemp= "\""+qcemp+"\"";
		qprdesig=qprdesig.replace(" ", "+");
		qprdesig ="\\"+ "\"" +qprdesig + "\\" +"\"";
	}else{
		qprdesig="*";
	}
	
	
    //String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";

	
	int start = 100 *(next);
	
    // imp String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+")"+exdata+"&fl=id,current_work_loc,dob,CreatedDate,EmailID,DesignationText,TotalExp,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,PresentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
    //String url=rootconfig.getResumeDataLocal()+"/select?q=TotalExp:"+qall+"&fl=id,current_work_loc,dob,CreatedDate,EmailID,DesignationText,TotalExp,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,PresentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	
	
   String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,html_data&start="+start+"&omitHeader=true&rows=100&wt=json&indent=true";

 // String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") A (CurrentCTC:"+qctc+") AND (TotalExps:"+qexp+")  AND (current_work_loc:"+qloc+") AND (PerferLocation:"+qploc+") AND (IndustryText:"+qindtyp+") AND (PresentEmployer:"+qcemp+") AND (previousEmployer:"+qpemp+") AND (DesignationText:"+qcurdesig+") AND (previousDesig:"+qprdesig+") AND (FunctionText:"+qfarea+")"+exdata+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,html_data&start="+start+"&omitHeader=true&rows=100&wt=json&indent=true";

    //TotalExps
    System.out.println(url);

	jsonData=restTemplate.getForObject(url, String.class);
	
	String[] rdata = highlight.split(",");
	int r = 0;
	/*for (r = 0; r < rdata.length; r++) {

		jsonData = jsonData.replaceAll(rdata[r], "<mark>" + rdata[r] + "</mark>").replaceAll("[<](/)?img[^>]*[>]", "");
	
	}*/
	 
	 }catch (Exception e) {
		e.printStackTrace();
		String str = e.getMessage();
		if (str.contains("400 Bad Request")) {
			return "{\"error\":\"please provide a valid data\"}";
		}
	}
	return jsonData;
}

@RequestMapping(value="/internalSearch")
public @ResponseBody String getInternalData(@RequestParam("qall") String qall,@RequestParam("qany") String qany,@RequestParam("qhiring") String qhiring,@RequestParam("qexclude") String qexclude,
		@RequestParam("qexp") String qexp,@RequestParam("qctc") String qctc,@RequestParam("qloc") String qloc, @RequestParam("qfarea") String qfarea, @RequestParam("qindustry") String qindustry, @RequestParam("qdesig") String qdesig ,
		@RequestParam("next") Integer next ){
	System.out.println("all query data::"+qall);
	
	if(qall.contains("#")){
		System.out.println("all query # symbol:::"+qall);
		//qall=qall.replace("#", "");
	}else{
		System.out.println("sorry there is no any # symbol");
	}
	String jsonData ="";
	String ss="";
	String s1="";
	String s2="";
	try{
	
	qany=qany.toLowerCase().replace("#", "%23");
	qany = "\"*" + qany.replace(",", "*\" OR \"*") + "*\""; 
	
	qall=qall.toLowerCase().replace("#", "%23");
	qall = "\"*" + qall.replace(",", "*\" AND \"*") + "*\"";
	System.out.println("qall::::"+qall);

	//String qctc= "2,6";
	//System.out.println("qctc::kkk:"+qctc.length());
	if (qctc.length() >= 3) {
		    //System.out.println("qctc:::"+qctc);
			qctc=qctc.replace(",", " TO ");
			//System.out.println("qpresentctc:::"+qctc);
			qctc= "["+qctc+"]";
			//System.out.println("qctc:::"+qctc);
			s1= " OR (PresentCTC:"+qctc+")";
			ss=s1;
	}	
	
	if (qexp.length() >= 3) {
			qexp=qexp.replace(",", " TO ");
			//System.out.println("qpresentctc:::"+qexp);
			qexp= "["+qexp+"]";
			//System.out.println("qexpireence:::"+qexp);
			s2= " OR (TotalExp:"+qexp+")";
			ss=s1+s2;
    }
	String ex="";
	String exdata="";
	qexclude=qexclude.toLowerCase().replace("#", "%23");
	if(qexclude.length()>=1){
			qexclude = "\"*" + qexclude.replace(",", "*\" OR \"*") + "*\"";
			ex= " NOT (_text_:"+qexclude+")";
			exdata=ex;
	}
	String hiringfor="";
	String hiring="";
	if(qhiring.length() >=1){
		qhiring=qhiring.toLowerCase().replace("#", "%23");
		hiring=" OR (html_data:"+qhiring+")";
		hiringfor=hiring;
	}
	
	qloc=qloc.toLowerCase().replace("#", "%23");
	qloc = "\"*" + qloc.replace(",", "*\" OR \"*") + "*\""; 
	
	qfarea=qfarea.toLowerCase().replace("#", "%23");
	qfarea = "\"*" + qfarea.replace(",", "*\" OR \"*") + "*\"";
	
	qindustry=qindustry.toLowerCase().replace("#", "%23");
	qindustry = "\"*" + qindustry.replace(",", "*\" OR \"*") + "*\"";
	
	qdesig=qdesig.toLowerCase().replace("#", "%23");
	qdesig = "\"*" + qdesig.replace(",", "*\" OR \"*") + "*\"";
    
	int start = 100 *(next);
    String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+")"+hiringfor+" OR (html_data:"+qany+")"+exdata+" OR (html_data:"+qloc+") OR (html_data:"+qfarea+") OR (html_data:"+qindustry+") OR (html_data:"+qdesig+")"+ss+"&fl=id,current_work_loc,dob,CreatedDate,EmailID,DesignationText,TotalExp,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,PresentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
    System.out.println(url);
	
    //OR (html_data:"+qhiring+")
    
	jsonData=restTemplate.getForObject(url, String.class);
	
	}catch (Exception e) {
		e.printStackTrace();
		String str = e.getMessage();
		/*if (str.contains("400 Bad Request")) {
			return "{\"error\":\"please provide a valid data\"}";
		}*/
	}
	return jsonData;
}




@RequestMapping(value="/allemail")
public @ResponseBody String getMAil(@RequestParam("allEmail") String allEmail, @RequestParam("next") Integer next) throws URISyntaxException, MalformedURLException{
	
	String	sort = "id" + " desc";
	//System.out.println("sort:::"+sort);
	int start = 100 *(next);
	//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";

	String subUrlEmcod=rootconfig.getResumeDataLocal()+"/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&rows=100&wt=json&indent=true";
    System.out.println(subUrlEmcod);
    
    URL url = new URL(subUrlEmcod);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
	return jsonData;	
}

@RequestMapping(value="/email")
public @ResponseBody String emailDetails(@RequestParam("search") String search) throws URISyntaxException, MalformedURLException{
	
	String  jsonData="";
try{	
	String	sort = "id" + " desc";
	
	//System.out.println("sort:::"+sort);
	if(search.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		search ="\"" +search +"\"";
		//search=search.replace("\"", "\"\"");
	}else{
		search="*";
	}

	System.out.println("search::::"+search);
	
	//int start = 100 *(next);
	//System.out.println("start::::"+start);
	//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
	//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	
	
	//System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
    //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
    
    String rooturl= rootconfig.getResumeDataEmail()+"/select?q=emails_data:"+search+"&fl=createdDate,emails_data,emailId&start=0&rows=100&omitHeader=true&wt=json&indent=true";
    
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
    System.out.println(rooturl);
    
   /* URL url = new URL(rooturl);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);*/
    
    jsonData=restTemplate.getForObject(rooturl, String.class);
    //System.out.println("jsonData:::"+jsonData);
    
}catch(Exception e){
   	e.printStackTrace();
   	String str= e.getMessage();
   	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
}
	return jsonData;
	
}




@RequestMapping(value="/attach")
public @ResponseBody String getAttachDetails(@RequestParam("resumeid") String resumeid) throws URISyntaxException, MalformedURLException{
	
	String  jsonData="";
try{	
	String	sort = "id" + " desc";
	
	//System.out.println("sort:::"+sort);
	if(resumeid.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		resumeid ="\"" +resumeid +"\"";
		//search=search.replace("\"", "\"\"");
	}else{
		resumeid="*";
	}
	
	System.out.println("resumeid::::"+resumeid);
	//int start = 100 *(next);
	//System.out.println("start::::"+start);
	//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
	//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	
	//System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
    //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    //System.out.println("aaa"+rootconfig.getResumeDataAttach());
    String rooturl= rootconfig.getResumeDataAttach()+"/select?q=rid:"+resumeid+"&fl=id,rid,oid,namej,namea,name,mobile,email,clstatus,assigned,createdby,modifiedby,enterdate,modidate,cv_sent_date&start=0&rows=100&omitHeader=true&wt=json&indent=true";
    
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
    System.out.println(rooturl);
    
    URL url = new URL(rooturl);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
     jsonData=restTemplate.getForObject(rooturl, String.class);
    //System.out.println("jsonData:::"+jsonData);
    
}catch(Exception e){
   	e.printStackTrace();
   	String str= e.getMessage();
   	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
}
	return jsonData;
	
}





//https://yamuna-1.globalhuntindia.com:8983/solr/attachaudit_shard1_replica2
	@RequestMapping(value="/attachAudit")
	public @ResponseBody String getOditDetails(@RequestParam("jobid") String jobid,@RequestParam("resumeid") String resumeid ) throws URISyntaxException, MalformedURLException{
		
		String  jsonData="";
	try{	
		String	sort = "id" + " desc";
		
		//System.out.println("sort:::"+sort);
		if(jobid.length() >=1){
			//search ="\\"+ "\"" +search + "\\" +"\"";
			jobid ="\"" +jobid +"\"";
			//search=search.replace("\"", "\"\"");
		}else{
			jobid="*";
		}
		if(resumeid.length() >=1){
			//search ="\\"+ "\"" +search + "\\" +"\"";
			resumeid ="\"" +resumeid +"\"";
			//search=search.replace("\"", "\"\"");
		}else{
			resumeid="*";
		}
		
		System.out.println("jobid::::"+jobid);
		System.out.println("resumeid::::"+resumeid);
		//int start = 100 *(next);
		//System.out.println("start::::"+start);
		//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
		//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
		
		//System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	    //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	    
	    String rooturl= rootconfig.getResumeDataLocal()+"/select?q=(job_id:"+jobid+")AND(ree_resume_id_c:"+resumeid+") &fl=parent_id,date_created,created_by,field_name,after_value_text,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	    
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
	    System.out.println(rooturl);
	    
	   /* URL url = new URL(rooturl);
	    String nullFragment = null;

	    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
	    url.getQuery(), nullFragment);*/
	    
	      jsonData=restTemplate.getForObject(rooturl, String.class);
	    //System.out.println("jsonData:::"+jsonData);
	    
	}catch(Exception e){
	   	e.printStackTrace();
	   	String str= e.getMessage();
	   	if (str.contains("400 Bad Request")) {
				return "{\"error\":\"data should not be blank\"}";
			}
	}
		return jsonData;
		
	}
	
	@RequestMapping(value="/candidates")
	public @ResponseBody String getDetail(@RequestParam("id") String id, @RequestParam("type") String type) throws URISyntaxException, MalformedURLException{
		String rooturl="";
		//e8e3db08
		
		//n8u3k7 //l6n2k4
		System.out.println("type::::"+type);
		//String  typ = "e8e3db08-dc39-48ea-a3db-08dc3958eafb";
		
		String	sort = "id" + " desc";
		//System.out.println("sort:::"+sort);
		if(id.length() >=1){
			//search ="\\"+ "\"" +search + "\\" +"\"";
			id ="\"" +id +"\"";
			//search=search.replace("\"", "\"\"");
		}
		
		//System.out.println("id::::"+id);
		//System.out.println("type::::"+type);
		
		//System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	    //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	    
		//int start = 100 *(next);
		//System.out.println("start::::"+start);
		//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
		//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
		
		System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        
       
	   if(type.equals("n8u3k7")){
		   //naukri
		   rooturl= rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	   }else if(type.equals("l6n2k4")){
		   //linkedin
		   //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
		   //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
		   rooturl= rootconfig.getResumeDataLinkedin()+"/select?q=id:"+id+"&fl=id,candidate_name,EmailID,PassportNo,Mobile,totalExps,DOB,AlternateEmailID,Gender,SkillsText,EducationText,CreatedUserID,CreatedDate,PerferLocation,LastUpdateDate,ViewCount,EmployeeID,ExpectedCTC,PresentCurrency,PresentEmployer,FunctionText,IndustryText,SubFunctionText,"
					+"DesignationText,EmpID,PinCode,UserName,previous_company_detail,logo_Contact,linkedin_can_id&start=0&rows=100&omitHeader=true&wt=json&indent=true";
				   
	   }/*else{
		   //naukri
		   rooturl= rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&wt=json&indent=true";

	   }*/
		
	    
	   
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
	    System.out.println(rooturl);
	    
	    URL url = new URL(rooturl);
	    String nullFragment = null;

	    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
	    url.getQuery(), nullFragment);
	    
	    String  jsonData=restTemplate.getForObject(U, String.class);
	    //System.out.println("jsonData:::"+jsonData);
		return jsonData;
		
	}
	
	
	/* IMP @RequestMapping(value="/candidates")
	public @ResponseBody String getDetail(@RequestParam("id") String id, @RequestParam("type") String type) throws URISyntaxException, MalformedURLException{
		String rooturl="";
		
		//e8e3db08
		
		//n8u3k7 //l6n2k4
		System.out.println("type::::"+type);
		//String  typ = "e8e3db08-dc39-48ea-a3db-08dc3958eafb";
		
		String	sort = "id" + " desc";
		//System.out.println("sort:::"+sort);
		if(id.length() >=1){
			//search ="\\"+ "\"" +search + "\\" +"\"";
			id ="\"" +id +"\"";
			//search=search.replace("\"", "\"\"");
		}
		
		
		//System.out.println("id::::"+id);
		//System.out.println("type::::"+type);
		
		//System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	    //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	    
		//int start = 100 *(next);
		//System.out.println("start::::"+start);
		//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
		//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
	   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	   if(type.equals("n8u3k7")){
		   //naukri
		   rooturl= rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	   }else if(type.equals("l6n2k4")){
		   //linkedin
		   //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
		   //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
		   rooturl= rootconfig.getResumeDataLinkedin()+"/select?q=id:"+id+"&fl=id,candidate_name,EmailID,PassportNo,Mobile,totalExps,DOB,AlternateEmailID,Gender,SkillsText,EducationText,CreatedUserID,CreatedDate,PerferLocation,LastUpdateDate,ViewCount,EmployeeID,ExpectedCTC,PresentCurrency,PresentEmployer,FunctionText,IndustryText,SubFunctionText,"
					+"DesignationText,EmpID,PinCode,UserName,previous_company_detail,logo_Contact,linkedin_can_id&start=0&rows=100&omitHeader=true&wt=json&indent=true";
				   
	   }else{
		   //naukri
		   rooturl= rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&wt=json&indent=true";

	   }
		
	    
	   
	    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
	    System.out.println(rooturl);
	    
	    URL url = new URL(rooturl);
	    String nullFragment = null;

	    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
	    url.getQuery(), nullFragment);
	    
	    String  jsonData=restTemplate.getForObject(U, String.class);
	    //System.out.println("jsonData:::"+jsonData);
		return jsonData;
		
	}*/	
	
@RequestMapping(value="/canaudit")
public @ResponseBody String getCanAudit(@RequestParam("id") String id, @RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException{
	
	if(id.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		id ="\"" +id +"\"";
		//search=search.replace("\"", "\"\"");
	}
	System.out.println("id :: "+id);
	
	if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
 	}
 
	 String authString = headers.getFirst("authorization");

	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
           return "{\"error\":\"User not authenticated\"}";
      }
	 //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	 //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	String rooturl= rootconfig.getResumeDataCanAudit()+"/select?q=User_id:"+id+"&fl=id,column_Name,Old_Value,New_Value,User_Name,Updated_date,User_id,modifideBy&start=0&rows=100&omitHeader=true&wt=json&indent=true";
   
	// String rooturl= rootconfig.getResumeDataCanAudit()+"/select?q=rid:"+id+"&fl=id,rid,oid,namej,namea,name,mobile,email,ghstatus,clstatus,assigned,createdby,modifiedby,enterdate,cv_sent_date,modidate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 
	 System.out.println("CandidateAudit URL :: "+rooturl);
    
    URL url = new URL(rooturl);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;
	
}

//Candidate Attach History (collection Attach) 28-Feb-2018-----------

@RequestMapping(value="/candAttachHistory")
public @ResponseBody String getCandidateAttachHistory(@RequestParam("id") String id,@RequestParam("sort") String sort, @RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException{
		
	
	if(id.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		id ="\"" +id +"\"";
		//search=search.replace("\"", "\"\"");
	}
	
if (sort.equals("enterdatea")) {
		
		sort = "enterdate" + " asc";
	}
	else if (sort.equals("enterdated")) {
		
		sort = "enterdate" + " desc";
	}
	id=id.toLowerCase();

	
	
	//System.out.println("rid :: "+id);
	
	if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
 	}
 
	 String authString = headers.getFirst("authorization");

	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
           return "{\"error\":\"User not authenticated\"}";
      }
	 //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	 //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	//String rooturl= rootconfig.getResumeDataCanAudit()+"/select?q=rid:"+id+"&fl=id,column_Name,Old_Value,New_Value,User_Name,Updated_date,User_id,modifideBy&start=0&rows=100&omitHeader=true&wt=json&indent=true";
   
	 String attachURL= rootconfig.getAttachjobUrl()+"/select?q=rid:"+id+"&fl=id,rid,oid,namej,namea,name,mobile,email,ghstatus,clstatus,assigned,createdby,modifiedby,enterdate,cv_sent_date,modidate,drop_comment_c,dropp_c,status_change_date,status_outcome&start=0&rows=100&omitHeader=true&wt=json&indent=true&sort="+sort;
	 
	 System.out.println("CandidateAttachHistory attachURL :: "+attachURL);
    
    URL url = new URL(attachURL);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;
	
}
// attach end


// Call API behalf of Mobile Number 28-Feb-2018-----------


@RequestMapping(value="/call")
public @ResponseBody String getCanndidateCall(@RequestParam("mobile") String mobile,@RequestParam("sort") String sort, @RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException{
	
		
	if(mobile.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		mobile ="\"" +mobile +"\"";
		//search=search.replace("\"", "\"\"");
	}
	
if (sort.equals("enterdatea")) {
		
		sort = "date_start" + " asc";
	}
	else if (sort.equals("enterdated")) {
		
		sort = "date_start" + " desc";
	}
	//mobile=mobile.toLowerCase();
	
	
	//System.out.println("Calls  mobile(d) :: "+mobile);
	
	if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
 	}
 
	 String authString = headers.getFirst("authorization");

	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
           return "{\"error\":\"User not authenticated\"}";
      }
	 //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	 //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	//String rooturl= rootconfig.getResumeDataCanAudit()+"/select?q=rid:"+id+"&fl=id,column_Name,Old_Value,New_Value,User_Name,Updated_date,User_id,modifideBy&start=0&rows=100&omitHeader=true&wt=json&indent=true";
   
	 String callURL= rootconfig.getCall_shardUrl()+"/select?q=d:"+mobile+"&fl=names,enterdate,call_duration,createdby,date_start,id,modidate,d,namec,call_action,namea&start=0&rows=100&omitHeader=true&wt=json&indent=true&sort="+sort;
	 
	 System.out.println("Call URL :: "+callURL);
    
    URL url = new URL(callURL);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;
	
}


////////Email collection >>

@RequestMapping(value="/emails")
public @ResponseBody String getEmailShard(@RequestParam("emailID") String emailID,@RequestParam("sort") String sort, @RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException{
	
	
	 
		
	
	
	if(emailID.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		emailID ="\"" +emailID +"\"";
		//search=search.replace("\"", "\"\"");
	}
	
	
	
	if (sort.equals("enterdatea")) {
		
		sort = "date_sent" + " asc";
	}
	else if (sort.equals("enterdated")) {
		
		sort = "date_sent" + " desc";
	}

emailID=emailID.toLowerCase();


	
	
	//System.out.println("Emails   :: "+emailID);
	
	if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
 	}
 
	 String authString = headers.getFirst("authorization");

	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
           return "{\"error\":\"User not authenticated\"}";
      }
	 //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.8.0_111/jre/lib/security/cacerts"); 
	 //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");	
	//String rooturl= rootconfig.getResumeDataCanAudit()+"/select?q=rid:"+id+"&fl=id,column_Name,Old_Value,New_Value,User_Name,Updated_date,User_id,modifideBy&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String emailURL= rootconfig.getEmail_shardUrl()+"/select?q=from_addr:"+emailID+"OR to_addrs:"+emailID+"&fl=from_addr,to_addrs,cc_addrs,bcc_addrs,names,parent_type,namec,id,date_sent&start=0&rows=100&omitHeader=true&wt=json&indent=true&sort="+sort;
	 
	 System.out.println("Emails URL :: "+emailURL);
    
    URL url = new URL(emailURL);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;
	
}


//Hitech Report--------------------------------------------------------------

@RequestMapping(value="/report")
public @ResponseBody String getContactReport(@RequestParam("id") String id) throws MalformedURLException, URISyntaxException, JSONException{

//public @ResponseBody String getContactReport(@RequestParam("id") String id, @RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException, JSONException{
	/*if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
	}
	 String authString = headers.getFirst("authorization");
	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
         return "{\"error\":\"User not authenticated\"}";
    }*/
	 
	   // String rooturl= rootconfig.getResumeDataLocal()+"/select?q=(job_id:"+jobid+")AND(ree_resume_id_c:"+resumeid+") &fl=parent_id,date_created,created_by,field_name,after_value_text,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
///select?q=(id:"+id+")OR(namea:"+id+")&fl &fq=-status:Order
	 
// String url=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,html_data&start="+start+"&omitHeader=true&rows=100&wt=json&indent=true";

	//contact  accountid
	 String contactMsURL= rootconfig.getContactMs_shardUrl()+"/select?q=accountid:"+id+"&fl=firstname,midlename,lastname,account,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String jobMsURL= rootconfig.getJobMs_shardUrl()+"/select?q=accountid:"+id+"&fl=contactname,accountname,name,createdby,modifiedby,bdanchorname,id,createddate,lastmodifieddate,descriptionnote,ghjobcode&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String accountMsURL= rootconfig.getAccountMs_shardUrl()+"/select?q=accountid:"+id+"&fl=name,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate,parentid,anchorid,createdusername,modifiedusername,isaccount,status,industriesid,subclassificationids,superclassificationids,industrygroupids,productsids,servicesids,brandsids,processids,ghcompetetorids,accountpreferencids,domains&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String agreementMsURL= rootconfig.getAgreementMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,contactname,createdusername,modifiedusername,createddate,lastmodifieddate,description,deleted,agreementenddate,agreementstartdate,invoicegeneration,invoicegenerationdays,agreementrenewaldate,agreementreferencekey,fixedamounttype,fixedamountvalue,agreementamount_type,agreementamount_value,accountid,relatecontact,minimum,maximum,replaceguarantee,resumevalidity,paymentcreditperiod,minmax,fixedfee,feepercentage,createdby,modifiedby,anchorname,relatecontact,agreementid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String callMsURL= rootconfig.getCallMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,parentname,accountname,createdusername,modifiedusername,duration,action,parentid,parenttype,status,direction,device,startdate,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,number,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String interviewMsURL= rootconfig.getInterviewMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,candidate_name,reresume_id,candidatename,accountname,contactname,jobname,jobid,PresentEmployer,EmailID,Mobiles,modifiedusername,joblocation,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,contactid,attachid,candidate_name,&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String invoiceMsURL= rootconfig.getInvoiceMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,offerid,candidatename,accountname,representativemail,invoicecode,duedate,representative,representativeid,aprepresentative,aprepresentativemailid,dateofinvoice,deteofjoining,billablectc,offerdesignation,locationoffer,invoicenumber,billingaddress,shipppingaddress,createddate,lastmodifieddate,createdby,modifiedby,anchorname,agreementid,offeremployeeID,offerapplicantID,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String offerMsURL= rootconfig.getOfferMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,name,accountname,namec,email,Mobile,candiId,createdby,modifiedby,anchorname,clientjobcode,jobName,status,raidytoinvoice,billraisday,offeredctc,ctccurrency,designation,dateofoffer,offeredctc,dateofjoining,statusoutcome,employeeCode,projectedrevenue,locationoffered,linemanager,buid,NSNid,ShareHolder,billablectc,agreementfixedfee,totalbillablectc,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String meetingMsURL= rootconfig.getMeetingMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,parentname,location,parenttype,contactname,leaduser,status,createdby,modifiedby,anchorname,action,startdate,enddate,direction,outcome,parentid,parenttype,createddate,lastmodifieddate,attendeesusers,inviteesusers,descriptions&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String interviewStagesMsURL= rootconfig.getInterviewStagesMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,candidatename,createddate,lastmodifieddate,modifiedby,createdby,description,accountname,jobname,jobid,joblocation,contactname,anchorname,status,venueaddress,startdate,enddate,interviewername&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String emailMsURL= rootconfig.getEmailMs_shardUrl()+"/select?q=parentid:"+id+"&fl=id,namec,sentdate,lastmodifieddate,parenttype,mailfromaddress,mailtoaddress,parentid,anchorname,modifiedby,createdby,subject,entryid,resumeid,blobid,volumeid&start=0&rows=100&omitHeader=true&wt=json&indent=true";

	 //String attachAuditMsURL= rootconfig.getAttachAuditMs_shardUrl()+"/select?q=parentid:"+id+"&fl=id,parentid,createddate,createdby,fieldname,beforevaluetext,aftervaluetext,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String offerCreditMsURL= rootconfig.getOfferCreditMs_shardUrl()+"/select?q=id:"+id+"&fl=id,offerid,createddate,lastmodifieddate,createdby,modifiedby,userid,username,creditamount,projectionamount,userrole,roleid,rolegroup&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String attachMsURL= rootconfig.getAttachMsjobUrl()+"/select?q=job_id:"+id+"&fl=id,ree_resume_id_c,jobid,jobname,accountname,name,mobile,email,ghstatus_text,clientstatus_texts,anchorname,createdby,modifiedby,date_entered,cv_sent_date,date_modified,assigned_user_id,drop_comment_c,dropp_c,without_id,ghstatus_id,clientstatus_id,status_outcome,status_change_date,change_reason,page_up_id,job_id,jobid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String candidateMsURL= rootconfig.getCandidateMs_shardUrl()+"/select?q=id:"+id+"&fl=id,EmailID,candidate_name,Mobile,PresentCTC,TotalExp,DOB,SkillsText,PresentEmployer,PresentEmployer,LastUpdateDate,current_ctc_money,CreatedDate,previous_company_detail&start=0&rows=100&omitHeader=true&wt=json&indent=true";


	 /* URL url = new URL(contactMsURL);
   String nullFragment = null;
   URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
   url.getQuery(), nullFragment);*/
  
  String  jsonDataContact=restTemplate.getForObject(contactMsURL, String.class);
  JSONObject jsonObjContact=(JSONObject) new JSONObject(jsonDataContact).get("response");
  //System.out.println("jsonObjContact >> "+jsonObjContact);
  Integer countContact=(Integer) jsonObjContact.get("numFound");
  System.out.println("countContact >> "+countContact);
 
  String  jsonDataJob=restTemplate.getForObject(jobMsURL, String.class);
  JSONObject jsonObjJob=(JSONObject) new JSONObject(jsonDataJob).get("response");
  //System.out.println("jsonObjJob >> "+jsonObjJob);
  Integer countJob=(Integer) jsonObjJob.get("numFound");
  System.out.println("countJob >> "+countJob);
  
  String  jsonDataAccount=restTemplate.getForObject(accountMsURL, String.class);
  JSONObject jsonObjAccount=(JSONObject) new JSONObject(jsonDataAccount).get("response");
  //System.out.println("jsonObjAccount >> "+jsonObjAccount);
  Integer countAccount=(Integer) jsonObjAccount.get("numFound");
  System.out.println("countAccount >> "+countAccount);
  
  String  jsonDataAgreement=restTemplate.getForObject(agreementMsURL, String.class);
  JSONObject jsonObjAgreement=(JSONObject) new JSONObject(jsonDataAgreement).get("response");
 // System.out.println("jsonObjAgreement >> "+jsonObjAgreement);
  Integer countAgreement=(Integer) jsonObjAgreement.get("numFound");
  System.out.println("countAgreement >> "+countAgreement);
 
  /*String  jsonDataAttach=restTemplate.getForObject(attachMsURL, String.class);
  JSONObject jsonObjAttach=(JSONObject) new JSONObject(jsonDataAttach).get("response");
  System.out.println("jsonObjAttach >> "+jsonObjAttach);
  Integer countAttach=(Integer) jsonObjAttach.get("numFound");
  System.out.println("countAttach >> "+countAttach);*/
  
  String  jsonDataCall=restTemplate.getForObject(callMsURL, String.class);
  JSONObject jsonObjCall=(JSONObject) new JSONObject(jsonDataCall).get("response");
  //System.out.println("jsonObjCall >> "+jsonObjCall);
  Integer countCall=(Integer) jsonObjCall.get("numFound");
  System.out.println("countCall >> "+countCall);
  
  String  jsonDataEmail=restTemplate.getForObject(emailMsURL, String.class);
  JSONObject jsonObjEmail=(JSONObject) new JSONObject(jsonDataEmail).get("response");
 // System.out.println("jsonObjEmail >> "+jsonObjEmail);
  Integer countEmail=(Integer) jsonObjEmail.get("numFound");
  System.out.println("countEmail >> "+countEmail);
  
  String  jsonDataInterview=restTemplate.getForObject(interviewMsURL, String.class);
  JSONObject jsonObjInterview=(JSONObject) new JSONObject(jsonDataInterview).get("response");
  //System.out.println("jsonObjInterview >> "+jsonObjInterview);
  Integer countInterview=(Integer) jsonObjInterview.get("numFound");
  System.out.println("countInterview >> "+countInterview);
  
  String  jsonDataInvoice=restTemplate.getForObject(invoiceMsURL, String.class);
  JSONObject jsonObjInvoice=(JSONObject) new JSONObject(jsonDataInvoice).get("response");
 // System.out.println("jsonObjInvoice >> "+jsonObjInvoice);
  Integer countInvoice=(Integer) jsonObjInvoice.get("numFound");
  System.out.println("countInvoice >> "+countInvoice);
  
  /*String  jsonDataCandidate=restTemplate.getForObject(candidateMsURL, String.class);
  JSONObject jsonObjCandidate=(JSONObject) new JSONObject(jsonDataCandidate).get("response");
  System.out.println("jsonObjCandidate >> "+jsonObjCandidate);
  Integer countCandidate=(Integer) jsonObjCandidate.get("numFound");
  System.out.println("countCandidate >> "+countCandidate);*/
  
  String jsonDataOffer=restTemplate.getForObject(offerMsURL, String.class);
  JSONObject jsonObjOffer= (JSONObject) new JSONObject(jsonDataOffer).get("response");
 // System.out.println("jsonObjOffer >> "+jsonObjOffer);
  Integer countOffer=(Integer) jsonObjOffer.getInt("numFound");
  System.out.println("countOffer >> "+countOffer);
  
  String jsonDataMeeting=restTemplate.getForObject(meetingMsURL, String.class);
  JSONObject jsonObjMeeting= (JSONObject) new JSONObject(jsonDataMeeting).get("response");
  //System.out.println("jsonObjMeeting >> "+jsonObjMeeting);
  Integer countMeeting=(Integer) jsonObjMeeting.getInt("numFound");
  System.out.println("countMeeting >> "+countMeeting);
  
 /* String jsonDataAttachAudit=restTemplate.getForObject(attachAuditMsURL, String.class);
  JSONObject jsonObjAttachAudit= (JSONObject) new JSONObject(jsonDataAttachAudit).get("response");
  System.out.println("jsonObjAttachAudit >> "+jsonObjAttachAudit);
  Integer countAttachAudit=(Integer) jsonObjAttachAudit.getInt("numFound");
  System.out.println("countAttachAudit >> "+countAttachAudit);*/
  
  String jsonDataInterviewStages=restTemplate.getForObject(interviewStagesMsURL, String.class);
  JSONObject jsonObjInterviewStages= (JSONObject) new JSONObject(jsonDataInterviewStages).get("response");
 // System.out.println("jsonObjInterviewStages >> "+jsonObjInterviewStages);
  Integer countInterviewStages=(Integer) jsonObjInterviewStages.getInt("numFound");
  System.out.println("countInterviewStages >> "+countInterviewStages);
  
  /*String jsonDataOfferCredit=restTemplate.getForObject(offerCreditMsURL, String.class);
  JSONObject jsonObjOfferCredit= (JSONObject) new JSONObject(jsonDataOfferCredit).get("response");
  System.out.println("jsonObjOfferCredit >> "+jsonObjOfferCredit);
  Integer countOfferCredit=(Integer) jsonObjOfferCredit.getInt("numFound");
  System.out.println("countOfferCredit >> "+countOfferCredit);*/
  
  JSONObject jsonObject = new JSONObject();
  JSONObject jsonObjDocs = new JSONObject();
  JSONObject jsonObjResponse = new JSONObject();
  
  JSONArray array = new JSONArray();
  JSONArray array1 = new JSONArray();
 
  
  array.put(countContact);
  array.put(countJob);
  array.put(countAccount);
  array.put(countAgreement);
  //array.put(countAttach);
  array.put(countCall);
  array.put(countEmail);
  array.put(countInterview);
  array.put(countInvoice);
 // array.put(countCandidate);
  array.put(countOffer);
  array.put(countMeeting);
 // array.put(countAttachAudit);
  array.put(countInterviewStages);
  //array.put(countOfferCredit);
  
  
 jsonObject.accumulate("contact", countContact);
 jsonObject.accumulate("job", countJob);
 jsonObject.accumulate("account", countAccount);
 jsonObject.accumulate("agreement", countAgreement);
 //jsonObject.accumulate("attach", countAttach);
 jsonObject.accumulate("call", countCall);
 jsonObject.accumulate("email", countEmail);
 jsonObject.accumulate("interview", countInterview);
 jsonObject.accumulate("invoice", countInvoice);
// jsonObject.accumulate("candidate", countCandidate);
 jsonObject.accumulate("offer", countOffer);
 jsonObject.accumulate("meeting", countMeeting);
 //jsonObject.accumulate("attachAudit", countAttachAudit);
 jsonObject.accumulate("interviewStages", countInterviewStages);
// jsonObject.accumulate("offerCredit", countOfferCredit);
 
 array1.put(jsonObject);

 
 jsonObjDocs.put("docs", array1);
 jsonObjResponse.put("response", jsonObjDocs);
   System.out.println(jsonObjResponse);
	return jsonObjResponse.toString();
	
}

//contactMs only accountid
//Using Post Method RequestBody

@RequestMapping(value="/contactReportPost",method=RequestMethod.POST)
public @ResponseBody String contactMsReportPostMethod(@RequestParam("id") String id){
	String contactMsURL= rootconfig.getContactMs_shardUrl()+"/select?q=accountid:"+id+"&fl=firstname,midlename,lastname,account,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	System.out.println("Only contactMsURL >> "+contactMsURL); 
	restTemplate.setMessageConverters(Arrays.asList(new HttpMessageConverter[]{new FormHttpMessageConverter(),new StringHttpMessageConverter()}));
	//System.out.println("restTemplate >> "+restTemplate);
	MultiValueMap<String, Object> map2=new LinkedMultiValueMap<String,Object>();
	map2.add("id", id.toString());
	System.out.println("map22>>>> " +map2);
	String jsonDataContact=restTemplate.postForObject(contactMsURL, map2, String.class);
	
	 // System.out.println("Only jsonDataContact >> "+jsonDataContact);
	return jsonDataContact;
}


///End 
@RequestMapping(value="/contactReport")
public @ResponseBody String contactMsReport(@RequestParam("id") String id){
	String contactMsURL= rootconfig.getContactMs_shardUrl()+"/select?q=accountid:"+id+"&fl=firstname,midlename,lastname,account,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	System.out.println("Only contactMsURL >> "+contactMsURL); 
	String  jsonDataContact=restTemplate.getForObject(contactMsURL, String.class);
	 // System.out.println("Only jsonDataContact >> "+jsonDataContact);
	return jsonDataContact;
}
@RequestMapping(value="/jobReport")
public @ResponseBody String jobMsReport(@RequestParam("id")String id){
	 String jobMsURL= rootconfig.getJobMs_shardUrl()+"/select?q=accountid:"+id+"&fl=contactname,accountname,name,createdby,modifiedby,bdanchorname,id,createddate,lastmodifieddate,descriptionnote,ghjobcode&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only jobMsURL >> "+jobMsURL);
	 String jsonDataJob=restTemplate.getForObject(jobMsURL, String.class);
	//System.out.println("Only jsonDataJob >> "+jsonDataJob);
	return jsonDataJob;
}
@RequestMapping(value="/accountReport")
public @ResponseBody String accountMsReport(@RequestParam("id")String id){
	 String accountMsURL= rootconfig.getAccountMs_shardUrl()+"/select?q=accountid:"+id+"&fl=name,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate,parentid,anchorid,createdusername,modifiedusername,isaccount,status,industriesid,subclassificationids,superclassificationids,industrygroupids,productsids,servicesids,brandsids,processids,ghcompetetorids,accountpreferencids,domains&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only accountMsURL >> "+accountMsURL);
	 String jsonDataAccount=restTemplate.getForObject(accountMsURL, String.class);
	 //System.out.println("Only jsonDataAccount >> "+jsonDataAccount);
	return jsonDataAccount;
}
@RequestMapping(value="/agreementReport")
public @ResponseBody String agreementMsReport(@RequestParam("id")String id){
	 String agreementMsURL= rootconfig.getAgreementMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,contactname,createdusername,modifiedusername,createddate,lastmodifieddate,description,deleted,agreementenddate,agreementstartdate,invoicegeneration,invoicegenerationdays,agreementrenewaldate,agreementreferencekey,fixedamounttype,fixedamountvalue,agreementamount_type,agreementamount_value,accountid,relatecontact,minimum,maximum,replaceguarantee,resumevalidity,paymentcreditperiod,minmax,fixedfee,feepercentage,createdby,modifiedby,anchorname,relatecontact,agreementid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only agreementMsURL >> "+agreementMsURL);
	 String jsonDataAgreement=restTemplate.getForObject(agreementMsURL, String.class);
	// System.out.println("Only jsonDataAgreement >> "+jsonDataAgreement);
	return jsonDataAgreement;
}
@RequestMapping(value="/attachReport")
public @ResponseBody String attachMsReport(@RequestParam("id")String id){
	String attachMsURL= rootconfig.getAttachMsjobUrl()+"/select?q=job_id:"+id+"&fl=id,ree_resume_id_c,jobid,jobname,accountname,name,mobile,email,ghstatus_text,clientstatus_texts,anchorname,createdby,modifiedby,date_entered,cv_sent_date,date_modified,assigned_user_id,drop_comment_c,dropp_c,without_id,ghstatus_id,clientstatus_id,status_outcome,status_change_date,change_reason,page_up_id,job_id,jobid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only attachMsURL >> "+attachMsURL);
	 String jsonDataAttach=restTemplate.getForObject(attachMsURL, String.class);
	// System.out.println("Only jsonDataAgreemetn >> "+jsonDataAgreement);
	return jsonDataAttach;
}
@RequestMapping(value="/callReport")
public @ResponseBody String callMsReport(@RequestParam("id")String id) throws MalformedURLException, URISyntaxException{
	 String callMsURL= rootconfig.getCallMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,parentname,accountname,createdusername,modifiedusername,duration,action,parentid,parenttype,status,direction,device,startdate,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,number,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only callMsURL >> "+callMsURL);
	 URL url= new URL(callMsURL);
	 String nullFragment=null;
	 URI u=new URI(url.getProtocol(),url.getUserInfo(),url.getHost(),url.getPort(),url.getPath(),url.getQuery(),nullFragment);
	 //System.out.println("uuuuu >> "+u);
	 String jsonDataCall=restTemplate.getForObject(u, String.class);
	// System.out.println("jsonDataCall >> "+jsonDataCall);
	return jsonDataCall;
}
@RequestMapping(value="/emailReport")
public @ResponseBody String emailMsReport(@RequestParam("id")String id) throws MalformedURLException, URISyntaxException{
	String emailMsURL= rootconfig.getEmailMs_shardUrl()+"/select?q=parentid:"+id+"&fl=id,namec,sentdate,lastmodifieddate,parenttype,mailfromaddress,mailtoaddress,parentid,anchorname,modifiedby,createdby,subject,entryid,resumeid,blobid,volumeid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only emailMsURL >> "+emailMsURL);
	 String nullFragment=null;
	 URL url=new URL(emailMsURL);
	 URI u=new URI(url.getProtocol(),url.getUserInfo(),url.getHost(),url.getPort(),url.getPath(),url.getQuery(),nullFragment);
	// System.out.println("uuuu >> "+u);
	 String jsonDataEmail=restTemplate.getForObject(u, String.class);
	return jsonDataEmail;
}
@RequestMapping(value="/interviewReport")
public @ResponseBody String interviewMsReport(@RequestParam("id")String id){
	 String interviewMsURL= rootconfig.getInterviewMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,candidate_name,reresume_id,candidatename,accountname,contactname,jobname,jobid,PresentEmployer,EmailID,Mobiles,modifiedusername,joblocation,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,contactid,attachid,candidate_name,&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only interviewMsURL >> "+interviewMsURL);
	 String jsonDataInterview=restTemplate.getForObject(interviewMsURL, String.class);
	return jsonDataInterview;
}
@RequestMapping(value="/invoiceReport")
public @ResponseBody String invoiceMsReport(@RequestParam("id")String id){
	 String invoiceMsURL= rootconfig.getInvoiceMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,offerid,candidatename,accountname,representativemail,invoicecode,duedate,representative,representativeid,aprepresentative,aprepresentativemailid,dateofinvoice,deteofjoining,billablectc,offerdesignation,locationoffer,invoicenumber,billingaddress,shipppingaddress,createddate,lastmodifieddate,createdby,modifiedby,anchorname,agreementid,offeremployeeID,offerapplicantID,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only invoiceMsURL >> "+invoiceMsURL);
	 String jsonDataInvoice=restTemplate.getForObject(invoiceMsURL, String.class);
	return jsonDataInvoice;
}
@RequestMapping(value="/candidateReport")
public @ResponseBody String candidateMsReport(@RequestParam("id")String id){
	 String candidateMsURL= rootconfig.getCandidateMs_shardUrl()+"/select?q=id:"+id+"&fl=id,EmailID,candidate_name,Mobile,PresentCTC,TotalExp,DOB,SkillsText,PresentEmployer,PresentEmployer,LastUpdateDate,current_ctc_money,CreatedDate,previous_company_detail&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only candidateMsURL >> "+candidateMsURL);
	 String jsonDataCandidate=restTemplate.getForObject(candidateMsURL, String.class);
	return jsonDataCandidate;
}
@RequestMapping(value="/offerReport")
public @ResponseBody String offerMsReport(@RequestParam("id")String id){
	 String offerMsURL= rootconfig.getOfferMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,name,accountname,namec,email,Mobile,candiId,createdby,modifiedby,anchorname,clientjobcode,jobName,status,raidytoinvoice,billraisday,offeredctc,ctccurrency,designation,dateofoffer,offeredctc,dateofjoining,statusoutcome,employeeCode,projectedrevenue,locationoffered,linemanager,buid,NSNid,ShareHolder,billablectc,agreementfixedfee,totalbillablectc,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only offerMsURL >> "+offerMsURL);
	 String jsonDataOffer=restTemplate.getForObject(offerMsURL, String.class);
	 return jsonDataOffer;
}
@RequestMapping(value="/meetingReport")
public @ResponseBody String meetingMsReport(@RequestParam("id")String id){
	 String meetingMsURL= rootconfig.getMeetingMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,parentname,location,parenttype,contactname,leaduser,status,createdby,modifiedby,anchorname,action,startdate,enddate,direction,outcome,parentid,parenttype,createddate,lastmodifieddate,attendeesusers,inviteesusers,description&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only meetingMsURL >> "+meetingMsURL);
	 String jsonDataMeeting=restTemplate.getForObject(meetingMsURL, String.class);
	 return jsonDataMeeting;
}
@RequestMapping(value="/attachAuditReport")
public @ResponseBody String attachAuditMsReport(@RequestParam("id")String id){
	 String attachAuditMsURL= rootconfig.getAttachAuditMs_shardUrl()+"/select?q=parentid:"+id+"&fl=id,parentid,createddate,createdby,fieldname,beforevaluetext,aftervaluetext,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only attachAuditMsURL >> "+attachAuditMsURL);
	 String jsonDataAttachAudit=restTemplate.getForObject(attachAuditMsURL, String.class);
	return jsonDataAttachAudit;
}
@RequestMapping(value="/interviewStagesReport")
public @ResponseBody String interviewStagesMsReport(@RequestParam("id")String id){
	 String interviewStagesMsURL= rootconfig.getInterviewStagesMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,candidatename,createddate,lastmodifieddate,modifiedby,createdby,description,accountname,jobname,jobid,joblocation,contactname,anchorname,status,venueaddress,startdate,enddate,interviewername&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only interviewStagesMsURL >> "+interviewStagesMsURL);
	 String jsonDataInterviewStages=restTemplate.getForObject(interviewStagesMsURL, String.class);
	 return jsonDataInterviewStages;
}
@RequestMapping(value="/offerCreditReport")
public @ResponseBody String offerCreditMsReport(@RequestParam("id")String id){
	 String offerCreditMsURL= rootconfig.getOfferCreditMs_shardUrl()+"/select?q=id:"+id+"&fl=id,offerid,createddate,lastmodifieddate,createdby,modifiedby,userid,username,creditamount,projectionamount,userrole,roleid,rolegroup&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("Only offerCreditMsURL >> "+offerCreditMsURL);
	 String jsonDataOfferCredit=restTemplate.getForObject(offerCreditMsURL, String.class);
	return jsonDataOfferCredit;
}


//13
@RequestMapping(value="/jobidCount")
public @ResponseBody String jobIdCount(@RequestParam("jobid")String jobid) throws JSONException{
	 String attachMsURL= rootconfig.getAttachMsjobUrl()+"/select?q=jobid:"+jobid+"&fl=id,ree_resume_id_c,jobid,jobname,accountname,name,mobile,email,ghstatus_text,clientstatus_texts,anchorname,createdby,modifiedby,date_entered,cv_sent_date,date_modified,assigned_user_id,drop_comment_c,dropp_c,without_id,ghstatus_id,clientstatus_id,status_outcome,status_change_date,change_reason,page_up_id,job_id,jobid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String attachAuditMsURL= rootconfig.getAttachAuditMs_shardUrl()+"/select?q=job_id:"+jobid+"&fl=id,parentid,createddate,createdby,fieldname,beforevaluetext,aftervaluetext,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String interviewMsURL= rootconfig.getInterviewMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,candidate_name,reresume_id,candidatename,accountname,contactname,jobname,jobid,PresentEmployer,EmailID,Mobiles,modifiedusername,joblocation,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,contactid,attachid,candidate_name,&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String interviewStagesMsURL= rootconfig.getInterviewStagesMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,anchorid,contactid,attachid,reresume_id,statusid,interviewid,candidatename,createddate,lastmodifieddate,modifiedby,createdby,description,accountname,jobname,jobid,joblocation,contactname,anchorname,status,venueaddress,startdate,enddate,interviewername&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String jobMsURL= rootconfig.getJobMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=hrcontactid,hmcontactid,industrygroupid,interviewvenueid,bussinessdivisionid,functionalgroupid,clientsheetid,mailbodytemplateid,deliveryanchorid,contactname,clientportalid,accountid,indirecttargetemployerid,directtargetemployerid,clientccmailid,clientbccmailid,classificaionid,subclassificationid,superclassificationid,accountname,name,createdby,modifiedby,bdanchorname,id,createddate,lastmodifieddate,descriptionnote,ghjobcode&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 String offerMsURL= rootconfig.getOfferMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,name,accountname,namec,email,Mobile,candiId,createdby,modifiedby,anchorname,clientjobcode,jobName,status,raidytoinvoice,billraisday,offeredctc,ctccurrency,designation,dateofoffer,offeredctc,dateofjoining,statusoutcome,employeeCode,projectedrevenue,locationoffered,linemanager,buid,NSNid,ShareHolder,billablectc,agreementfixedfee,totalbillablectc,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";

	
	//String contactMsURL= rootconfig.getContactMs_shardUrl()+"/select?q=accountid:"+id+"&fl=firstname,midlename,lastname,account,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String accountMsURL= rootconfig.getAccountMs_shardUrl()+"/select?q=accountid:"+id+"&fl=name,createdby,modifiedby,anchorname,id,createddate,lastmodifieddate,parentid,anchorid,createdusername,modifiedusername,isaccount,status,industriesid,subclassificationids,superclassificationids,industrygroupids,productsids,servicesids,brandsids,processids,ghcompetetorids,accountpreferencids,domains&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 //String agreementMsURL= rootconfig.getAgreementMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,contactname,createdusername,modifiedusername,createddate,lastmodifieddate,description,deleted,agreementenddate,agreementstartdate,invoicegeneration,invoicegenerationdays,agreementrenewaldate,agreementreferencekey,fixedamounttype,fixedamountvalue,agreementamount_type,agreementamount_value,accountid,relatecontact,minimum,maximum,replaceguarantee,resumevalidity,paymentcreditperiod,minmax,fixedfee,feepercentage,createdby,modifiedby,anchorname,relatecontact,agreementid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String callMsURL= rootconfig.getCallMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,parentname,accountname,createdusername,modifiedusername,duration,action,parentid,parenttype,status,direction,device,startdate,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,number,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String emailMsURL= rootconfig.getEmailMs_shardUrl()+"/select?q=parentid:"+id+"&fl=id,namec,sentdate,lastmodifieddate,parenttype,mailfromaddress,mailtoaddress,parentid,anchorname,modifiedby,createdby,subject,entryid,resumeid,blobid,volumeid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 //String invoiceMsURL= rootconfig.getInvoiceMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,offerid,candidatename,accountname,representativemail,invoicecode,duedate,representative,representativeid,aprepresentative,aprepresentativemailid,dateofinvoice,deteofjoining,billablectc,offerdesignation,locationoffer,invoicenumber,billingaddress,shipppingaddress,createddate,lastmodifieddate,createdby,modifiedby,anchorname,agreementid,offeremployeeID,offerapplicantID,anchorid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String candidateMsURL= rootconfig.getCandidateMs_shardUrl()+"/select?q=id:"+id+"&fl=id,EmailID,candidate_name,Mobile,PresentCTC,TotalExp,DOB,SkillsText,PresentEmployer,PresentEmployer,LastUpdateDate,current_ctc_money,CreatedDate,previous_company_detail&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String meetingMsURL= rootconfig.getMeetingMs_shardUrl()+"/select?q=accountid:"+id+"&fl=id,accountname,parentname,location,parenttype,contactname,leaduser,status,createdby,modifiedby,anchorname,action,startdate,enddate,direction,outcome,parentid,parenttype,createddate,lastmodifieddate,attendeesusers,inviteesusers,descriptions&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	// String offerCreditMsURL= rootconfig.getOfferCreditMs_shardUrl()+"/select?q=id:"+id+"&fl=id,offerid,createddate,lastmodifieddate,createdby,modifiedby,userid,username,creditamount,projectionamount,userrole,roleid,rolegroup&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	
	 
	  JSONObject jsonObject = new JSONObject();
	  JSONObject jsonObjDocs = new JSONObject();
	  JSONObject jsonObjResponse = new JSONObject();
	  JSONArray array = new JSONArray();
	  JSONArray array1 = new JSONArray();
	 
	  String  jsonDataAttach=restTemplate.getForObject(attachMsURL, String.class);
	  JSONObject jsonObjAttach=(JSONObject) new JSONObject(jsonDataAttach).get("response");
	  //System.out.println("jsonObjAttach >> "+jsonObjAttach);
	  Integer countAttach=(Integer) jsonObjAttach.get("numFound");
	  System.out.println("countAttach >> "+countAttach);
	  array.put(countAttach);
	  jsonObject.accumulate("attach", countAttach);
	  
	 String jsonDataAttachAudit=restTemplate.getForObject(attachAuditMsURL, String.class);
	 JSONObject jsonObjAttachAudit=(JSONObject) new JSONObject(jsonDataAttachAudit).get("response");
	// System.out.println("jsonObjAttachAudit >> "+jsonObjAttachAudit);
	 Integer countAttachAudit=jsonObjAttachAudit.getInt("numFound");
	 System.out.println("countAttachAudit >> "+countAttachAudit);
	 array.put(countAttachAudit);
	 jsonObject.accumulate("attachAudit", countAttachAudit);
	 
	 String jsonDataInterview=restTemplate.getForObject(interviewMsURL, String.class);
	 JSONObject jsonObjInterview=(JSONObject) new JSONObject(jsonDataInterview).get("response");
	 //System.out.println("jsonObjInterview >> "+jsonDataInterview);
	 Integer countInterview=jsonObjInterview.getInt("numFound");
	 System.out.println("countInterview >> "+countInterview);
	 array.put(countInterview);
	 jsonObject.accumulate("inteview", countInterview);
	 
	 String jsonDataJob=restTemplate.getForObject(jobMsURL, String.class);
	 JSONObject jsonObjJob=(JSONObject) new JSONObject(jsonDataJob).get("response");
	// System.out.println("jsonObjJob >> "+jsonObjJob);
	 Integer countJob=jsonObjJob.getInt("numFound");
	 System.out.println("countJob >> "+countJob);
	 array.put(countJob);
	 jsonObject.accumulate("job", countJob);
	 
	 String jsonDataInterviewStages=restTemplate.getForObject(interviewStagesMsURL, String.class);
	 JSONObject jsonObjInterviewStages=(JSONObject) new JSONObject(jsonDataInterviewStages).get("response");
	 //System.out.println("jsonObjInterviewStages >> "+jsonObjInterviewStages);
	 Integer countInterviewStages=jsonObjInterviewStages.getInt("numFound");
	 System.out.println("countInterviewStages >> "+countInterviewStages);
	 array.put(countInterviewStages);
	 jsonObject.accumulate("interviewStages", countInterviewStages);
	 
	 String jsonDataOffer=restTemplate.getForObject(offerMsURL, String.class);
	 JSONObject jsonObjOffer=(JSONObject) new JSONObject(jsonDataOffer).get("response");
	 //System.out.println("jsonObjOffer >> "+jsonObjOffer);
	 Integer countOffer=jsonObjOffer.getInt("numFound");
	 System.out.println("countOffer >> "+countOffer);
	 array.put(countOffer);
	 jsonObject.accumulate("offer", countOffer);
	 
	  array1.put(jsonObject);
	  jsonObjDocs.put("docs", array1);
	  jsonObjResponse.put("response", jsonObjDocs);
	  System.out.println("jsonObjResponse >> "+jsonObjResponse);
	  return jsonObjResponse.toString();
	
}

//17
@RequestMapping(value="/attachjid")
public @ResponseBody String attachMsJobId(@RequestParam("jobid")String jobid){
	String attachMsURL= rootconfig.getAttachMsjobUrl()+"/select?q=jobid:"+jobid+"&fl=id,ree_resume_id_c,jobid,jobname,accountname,name,mobile,email,ghstatus_text,clientstatus_texts,anchorname,createdby,modifiedby,date_entered,cv_sent_date,date_modified,assigned_user_id,drop_comment_c,dropp_c,without_id,ghstatus_id,clientstatus_id,status_outcome,status_change_date,change_reason,page_up_id,job_id,jobid&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	System.out.println("attachMsURL >> "+attachMsURL);
	String jsonDataAttach=restTemplate.getForObject(attachMsURL, String.class);
	return jsonDataAttach;
	
}

@RequestMapping(value="/attachAuditjid")
public @ResponseBody String attachAuditMsJobId(@RequestParam("jobid")String jobid){
	 String attachAuditMsURL= rootconfig.getAttachAuditMs_shardUrl()+"/select?q=job_id:"+jobid+"&fl=id,parentid,createddate,createdby,fieldname,beforevaluetext,aftervaluetext,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("attachAuditMsURL >> "+attachAuditMsURL);
	 String jsonDataAttachAudit=restTemplate.getForObject(attachAuditMsURL, String.class);
	 return jsonDataAttachAudit;
}

@RequestMapping(value="/interviewjid")
public @ResponseBody String interivewMsJobId(@RequestParam("jobid")String jobid){
	 String interviewMsURL= rootconfig.getInterviewMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,candidate_name,reresume_id,candidatename,accountname,contactname,jobname,jobid,PresentEmployer,EmailID,Mobiles,modifiedusername,joblocation,createdby,modifiedby,anchorname,createddate,lastmodifieddate,accountid,contactid,attachid,candidate_name,&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("interviewMsURL >> "+interviewMsURL);
	 String jsonDataInterview=restTemplate.getForObject(interviewMsURL, String.class);
	return jsonDataInterview;
	
}

@RequestMapping(value="/interviewStagesjid")
public @ResponseBody String interviewStagesMsJobId(@RequestParam("jobid")String jobid){
	 String interviewStagesMsURL= rootconfig.getInterviewStagesMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,anchorid,contactid,attachid,reresume_id,statusid,interviewid,candidatename,createddate,lastmodifieddate,modifiedby,createdby,description,accountname,jobname,jobid,joblocation,contactname,anchorname,status,venueaddress,startdate,enddate,interviewername&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("interviewStagesMsURL >> "+interviewStagesMsURL);
	 String jsonDataInterviewStages=restTemplate.getForObject(interviewStagesMsURL, String.class);
	return jsonDataInterviewStages;
}

@RequestMapping(value="/jobjid")
public @ResponseBody String jobAttachReport(@RequestParam("jobid")String jobid){
	 
	 //String jobMsURL= rootconfig.getJobMs_shardUrl()+"/select?q="+id+"&fl=contactname,accountname,name,createdby,modifiedby,bdanchorname,id,createddate,lastmodifieddate,descriptionnote,ghjobcode&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	//String jobMsURL= rootconfig.getJobMs_shardUrl()+"/select?q=jobid :"+id+"&fl=contactname,accountname,name,createdby,modifiedby,bdanchorname,id,createddate,lastmodifieddate,descriptionnote,ghjobcode&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	String jobMsURL=rootconfig.getJobMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,name,lastmodifieddate,deleted,hrcontactid,hmcontactid,grade,ctccurrency,ctcfrom,ctcto,industrygroupid,industrygrouptext,numberofopening,jobtype,status,interviewvenueid,bussinessdivisionid,functionalgroupid,bdanchorid,ghjobcode,clientsheetid,clientsheetname,mailbodytemplateid,createdby,modifiedby,createddate,mspaccountid,mspaccountname,accountname,ismsp,primaryaccount,deliveryanchorid,deliveryanchorname,clientportalid,positionstatus,accountid,jobid,indirecttargetemployerid,directtargetemployerid,clientccmailid,clientbccmailid,classificaionid,subclassificationid,superclassificationid,productid,servicesid,tooltechnologiesid,ghccmailid,ghbccmailid,accountpreferencesid,functionalId,subfunctionalid,superfunctionalid,jobgradeid,locationid,industryid,account,contactname,midlename&start=0&rows=100&wt=json&omitHeader=true&indent=true";
	System.out.println("jobMsURL >> "+jobMsURL);
	 String jsonDataJob=restTemplate.getForObject(jobMsURL, String.class);
	return jsonDataJob;
}
@RequestMapping(value="/offerjid")
public @ResponseBody String offerMsJobId(@RequestParam("jobid")String jobid){
	 String offerMsURL= rootconfig.getOfferMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,name,accountname,namec,email,Mobile,candiId,createdby,modifiedby,anchorname,clientjobcode,jobName,status,raidytoinvoice,billraisday,offeredctc,ctccurrency,designation,dateofoffer,offeredctc,dateofjoining,statusoutcome,employeeCode,projectedrevenue,locationoffered,linemanager,buid,NSNid,ShareHolder,billablectc,agreementfixedfee,totalbillablectc,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	 System.out.println("offerMsURL >> "+offerMsURL);
	 String jsonDataOffer=restTemplate.getForObject(offerMsURL, String.class);
	 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
	return jsonDataOffer;
}


@RequestMapping(value="/elasticGet",method=RequestMethod.GET)
public @ResponseBody String elastiSearchGetMethod(/*@RequestParam("name")String name*/){
	// String offerMsURL= rootconfig.getOfferMs_shardUrl()+"/select?q=jobid:"+jobid+"&fl=id,name,accountname,namec,email,Mobile,candiId,createdby,modifiedby,anchorname,clientjobcode,jobName,status,raidytoinvoice,billraisday,offeredctc,ctccurrency,designation,dateofoffer,offeredctc,dateofjoining,statusoutcome,employeeCode,projectedrevenue,locationoffered,linemanager,buid,NSNid,ShareHolder,billablectc,agreementfixedfee,totalbillablectc,createddate,lastmodifieddate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?default_operator=AND&q=typeId:other+hitech&size=50&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?size=20&from=0";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?size=1&from=0";
	
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?default_operator=AND&q=fromMail:"+name+"&size=50&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";

	String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	System.out.println("ElasticSearch Data URL >> "+url);
	 String jsonElasticUrl=restTemplate.getForObject(url, String.class);
	 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
	 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
	return jsonElasticUrl;
}


@RequestMapping(value="/data")
public @ResponseBody String elastiMethod(@RequestParam("anykey")String anykey,@RequestParam("module")String module){
	try{//
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=fromMail:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//specific field query
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=blobId:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//Any field query
		anykey=anykey.toLowerCase();
		module=module.toLowerCase();
		if(module.equals("emaildataindex")){
			if(anykey.contains("@"))
				anykey=anykey.replaceAll("@", "%40");
			if(anykey.contains(":"))
				anykey=anykey.replaceAll(":", "%3A");
			String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q="+anykey+"&size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType,emlFile";  //
			System.out.println("emaildataindex URL Data  >> "+url);
			
			String jsonElasticUrl=restTemplate.getForObject(url, String.class);
			 //System.out.println(" emaildataindex JSON Data >>  "+jsonElasticUrl);
			 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
			return jsonElasticUrl;	
		}
		
		
		if(module.equals("calldataindex")){
			if(anykey.contains("@"))
				anykey=anykey.replaceAll("@", "%40");
			if(anykey.contains(":"))
				anykey=anykey.replaceAll(":", "%3A");
			String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/calldataindex/_search?q="+anykey+"&size=10&from=0&pretty=true&_source=id,callId,fromNumber,toNumber,userName,startTime,duration,parentId,parentName,parentType,callType,direction,device";
			System.out.println("calldataindex Data URL >> "+url);
			
			String jsonElasticUrl=restTemplate.getForObject(url, String.class);
			// System.out.println("calldataindex JSON Data >>  "+jsonElasticUrl);
			 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
			return jsonElasticUrl;	
		}
		
		
		
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
	return null;
}


@RequestMapping(value="/exacttext")
public @ResponseBody String fullTextExactData(@RequestParam("qany")String qany){
	try{//
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=fromMail:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//specific field query
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=blobId:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//Any field query
		/*if(qany.contains("@"))
			qany=qany.replaceAll("@", "%40");
		if(qany.contains(":"))
			qany=qany.replaceAll(":", "%3A");
		if (qany.contains("Contacts")) {
			
		}*/
		
		
		/* String dynamiCQuery="";
			
		 if(dochek4Null(qany)){
	         //qany= getAndwithData(qany,"html_data  OR EmailID OR Mobile","OR","");
	          String  mailany =URLEncoder.encode(qany, "UTF-8");
	          String  regex="[0-9]";
	          Pattern pattern = Pattern.compile(regex);
	          Matcher matcher = pattern.matcher(qany);
	          
	     	 if(mailany.contains("%40")){
	     		 //System.out.println("jjjjj");
	     		 qany= getAndwithData(qany,"toMail  OR fromMail","OR","");
	     		 qany= getAndwithData(qany,"ccMail  OR  bccMail ","OR","");
	     		 qany=qany.replace("*", ""); 
	     	 }else if(matcher.find()){
	     		     //System.out.println("qanyllllllllmmmmmllll");
	     		     qany= getAndwithData(qany,"Mobile","OR","");
	     		     qany=qany.replace("*", ""); 
	     	 }else{
	     		 //System.out.println("qanyllllllllllll"+qany);
	     		 qany= getAndwithData(qany,"","OR",""); //org
	     		 //System.out.println("hello any key::::"+qany);
	     	 }        
	          
	        if( dochek4Null(dynamiCQuery)){
		             // dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
	        	      dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
		             }else{
		            	  dynamiCQuery="("+qany+")";
		             }
	        if(qany.contains("Candidates")){
	        	dynamiCQuery=qany;
	        }
	          }
		*/
		
		
		String dynamiCQuery="";
		
		 if(dochek4Null(qany)){
	         //qany= getAndwithData(qany,"html_data  OR EmailID OR Mobile","OR","");
	          String  mailany =URLEncoder.encode(qany, "UTF-8");
	          String  regex="[0-9]";
	          Pattern pattern = Pattern.compile(regex);
	          Matcher matcher = pattern.matcher(qany);
	          
	     	 if(mailany.contains("%40")){
	     		 //System.out.println("jjjjj");
	     		 qany= getAndwithData(qany,"emlFile  OR toMail","OR","");
	     		 qany= getAndwithData(qany,"emlFile OR fromMail","OR","");
	     		 qany= getAndwithData(qany,"emlFile  OR ccMail","OR","");
	     		 qany= getAndwithData(qany,"emlFile  OR bccMail","OR","");
	     		
	     		 qany=qany.replace("*", ""); 
	     	 }else if(matcher.find()){
	     		     //System.out.println("qanyllllllllmmmmmllll");
	     		     qany= getAndwithData(qany,"emlFile OR fromMail","OR","");
	     		     qany= getAndwithData(qany,"emlFile  OR toMail","OR","");
		     		 qany= getAndwithData(qany,"emlFile  OR ccMail","OR","");
		     		 qany= getAndwithData(qany,"emlFile  OR bccMail","OR","");

	     		     qany=qany.replace("*", ""); 
	     	 }else{
	     		 //System.out.println("qanyllllllllllll"+qany);
	     		 qany= getAndwithData(qany,"emlFile","OR",""); //org
	     		 //System.out.println("hello any key::::"+qany);
	     		 qany= getAndwithData(qany,"sentDate","OR","");
	     		 qany= getAndwithData(qany,"subject","OR","");
	     		 qany= getAndwithData(qany,"volumeId","OR","");
	     		 qany= getAndwithData(qany,"blobId","OR","");
	     		 qany= getAndwithData(qany,"parentId","OR","");
	     		 qany= getAndwithData(qany,"parentType","OR",""); //org
	     		

	     	 }        
	          
	        if( dochek4Null(dynamiCQuery)){
		             // dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
	        	      dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
		             }else{
		            	  dynamiCQuery="("+qany+")";
		             }
	          }
		 // only for  allKeywords
	    /* if( dochek4Null(qall)){
	    	String  mail =URLEncoder.encode(qall, "UTF-8");
	    	String  regex="[0-9]";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(qany);
	    	
	    	if(mail.contains("%40")){
	    		 qall= getAndwithData(qall,"html_data  OR EmailID","AND","");
	    		 
	    		 //System.out.println("hi contains"+qall);
	       	     qall=qall.replace("*", ""); 
	       	     //System.out.println("hiqall contains"+qall);
	    	 } 
	    	  else if(matcher.find()){
	 		     //System.out.println("qanyllllllllmmmmmllll");
	 		     qany= getAndwithData(qany,"html_data OR Mobile","OR","");
	 		     qany=qany.replace("*", ""); 
	 		     
	 	     }else {
	    		 qall= getAndwithData(qall,"html_data","AND","");
	    		 qall=qall.replace("\"", "\"\"").replace("*", "");
	       	     //System.out.println("sorry contains");
	    	 }              
	    	
	      if( dochek4Null(dynamiCQuery)){
	            dynamiCQuery=dynamiCQuery + "AND"+ "("+qall+")";  //OR
	            }else{
	           	 dynamiCQuery="("+qall+")";
	            }
	     }
	   
	     // only fOR qexclude.
			if (dochek4Null(exclude)) {
				exclude = getAndwithData(exclude, "", "", "NOT");
	            // qexclude= getAndwithData(qexclude,"html_data","","NOT");
				System.out.println("qexclude::::" + exclude);
				if (dochek4Null(dynamiCQuery)) {
					dynamiCQuery = dynamiCQuery + "NOT" + "(" + exclude + ")";
				} else {
					dynamiCQuery = "(" + exclude + ")";
				}
			}  */
			
		
		
		
		
				
		String url2="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q="+dynamiCQuery+"&size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		System.out.println("ElasticSearch Data URL >> "+url2);
		
	       URL url = new URL(url2);
	 	   String nullFragment = null;
	       URI UOrig = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),url.getQuery(), nullFragment);

		
		String jsonElasticUrl=restTemplate.getForObject(UOrig, String.class);
		 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
		 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
		return jsonElasticUrl;	
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
	return null;
}


//hks//////////////////////////////////////////////

/*@RequestMapping(value ="/job")
public @ResponseBody String jobData(@RequestParam("qany")String qany,@RequestParam("qall")String qall, @RequestParam("exclude")String exclude,
		@RequestParam("indusgroup")String indusgroup,@RequestParam("industyp")String industyp,@RequestParam("ctc")String ctc,
		@RequestParam("exp")String exp,@RequestParam("grdc")String grdc,@RequestParam("skill")String skill) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException{
       
	//qany=&qall=&exclude=&indusgroup=&industyp=&ctc=&exp=&grdc=&skill=
	
	 String dynamiCQuery="";
	
	 if(dochek4Null(qany)){
         //qany= getAndwithData(qany,"html_data  OR EmailID OR Mobile","OR","");
          String  mailany =URLEncoder.encode(qany, "UTF-8");
          String  regex="[0-9]";
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(qany);
          
     	 if(mailany.contains("%40")){
     		 //System.out.println("jjjjj");
     		 qany= getAndwithData(qany,"html_data  OR EmailID","OR","");
     		 qany=qany.replace("*", ""); 
     	 }else if(matcher.find()){
     		     //System.out.println("qanyllllllllmmmmmllll");
     		     qany= getAndwithData(qany,"html_data OR Mobile","OR","");
     		     qany=qany.replace("*", ""); 
     	 }else{
     		 //System.out.println("qanyllllllllllll"+qany);
     		 qany= getAndwithData(qany,"html_data","OR",""); //org
     		 //System.out.println("hello any key::::"+qany);
     	 }        
          
        if( dochek4Null(dynamiCQuery)){
	             // dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
        	      dynamiCQuery=dynamiCQuery + "OR"+ "("+qany+")";
	             }else{
	            	  dynamiCQuery="("+qany+")";
	             }
          }
	 // only for  allKeywords
     if( dochek4Null(qall)){
    	String  mail =URLEncoder.encode(qall, "UTF-8");
    	String  regex="[0-9]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(qany);
    	
    	if(mail.contains("%40")){
    		 qall= getAndwithData(qall,"html_data  OR EmailID","AND","");
    		 
    		 //System.out.println("hi contains"+qall);
       	     qall=qall.replace("*", ""); 
       	     //System.out.println("hiqall contains"+qall);
    	 } 
    	  else if(matcher.find()){
 		     //System.out.println("qanyllllllllmmmmmllll");
 		     qany= getAndwithData(qany,"html_data OR Mobile","OR","");
 		     qany=qany.replace("*", ""); 
 		     
 	     }else {
    		 qall= getAndwithData(qall,"html_data","AND","");
    		 qall=qall.replace("\"", "\"\"").replace("*", "");
       	     //System.out.println("sorry contains");
    	 }              
    	
      if( dochek4Null(dynamiCQuery)){
            dynamiCQuery=dynamiCQuery + "AND"+ "("+qall+")";  //OR
            }else{
           	 dynamiCQuery="("+qall+")";
            }
     }
   
     // only fOR qexclude.
		if (dochek4Null(exclude)) {
			exclude = getAndwithData(exclude, "", "", "NOT");
            // qexclude= getAndwithData(qexclude,"html_data","","NOT");
			System.out.println("qexclude::::" + exclude);
			if (dochek4Null(dynamiCQuery)) {
				dynamiCQuery = dynamiCQuery + "NOT" + "(" + exclude + ")";
			} else {
				dynamiCQuery = "(" + exclude + ")";
			}
		}  
		
		
		// int start = 100 *(next);
		    
	       // String pQuery="(html_data:"+qall+") OR (html_data:"+qany+") OR (html_data:"+qbol+") OR (CurrentCTC:"+qctc+") OR (TotalExps:"+qexp+")  OR (current_work_loc:"+qloc+") OR (PerferLocation:"+qploc+") OR (IndustryText:"+qindtyp+") OR (PresentEmployer:"+qcemp+") OR (previousEmployer:"+qpemp+") OR (DesignationText:"+qcurdesig+") OR (previousDesig:"+qprdesig+") OR (FunctionText:"+qfarea+")"+exdata;
	      
	       //System.out.println("Parent Query:::::"+pQuery);
	       //String url=rootconfig.getResumeDataLocal()+"/select?q="+dynamiCQuery+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation&start="+start+"&rows=100&omitHeader=false&wt=json&indent=true";
	       String Rooturl=rootconfig.getResumeDataLive()+"/select?q="+dynamiCQuery+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	       System.out.println(Rooturl);
	       
	       URL url = new URL(Rooturl);
	 	   String nullFragment = null;
	       URI UOrig = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),url.getQuery(), nullFragment);
	       
	 		//System.out.println("Original Url:" + UOrig);
	 	String	jsonData = restTemplate.getForObject(UOrig, String.class);
	 
	        return jsonData;
      }

*/















//////hks end


@RequestMapping(value="/fullText")
public @ResponseBody String fullTextSearch(@RequestParam("toMail")String toMail,@RequestParam("fromMail")String fromMail/*,
		@RequestParam("blobId")String blobId,@RequestParam("typeId")String typeId,@RequestParam("parentId")String parentId,
		@RequestParam("parentType")String parentType,*/,@RequestParam("sentDate")String sentDate){
	try{
		
	if(toMail.contains("@"))
		toMail=toMail.replaceAll("@", "%40");
	if(fromMail.contains("@"))
		fromMail=fromMail.replaceAll("@", "%40");
	if(sentDate.contains(":"))
		sentDate=sentDate.replaceAll(":", "%3A");
	
	String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=toMail:"+toMail+"AND fromMail:"+fromMail+"AND sentDate:"+sentDate+" &size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q="+toMail+" &size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";

	
	System.out.println("ElasticSearch Data URL >> "+url);
	
	String jsonElasticUrl=restTemplate.getForObject(url, String.class);
	 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
	 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
	return jsonElasticUrl;	
}
catch (Exception e) {
	System.out.println(e.getMessage());
}
return null;
	
}

// onl OR Condition

@RequestMapping(value="/textor")
public @ResponseBody String textOrValue(@RequestParam("fieldtext")String fieldtext){
	try{
		
	if(fieldtext.contains("@"))
		fieldtext=fieldtext.replaceAll("@", "%40");
	if(fieldtext.contains("@"))
		fieldtext=fieldtext.replaceAll("@", "%40");
	if(fieldtext.contains(":"))
		fieldtext=fieldtext.replaceAll(":", "%3A");
	//	    String rooturl= rootconfig.getResumeDataLocal()+"/select?q=(job_id:"+jobid+")AND(ree_resume_id_c:"+resumeid+") &fl=parent_id,date_created,created_by,field_name,after_value_text,modifiedby,columnname,job_id,ree_resume_id_c&start=0&rows=100&omitHeader=true&wt=json&indent=true";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=toMail:"+fieldtext+"analyze_wildcard fromMail:"+fieldtext+"analyze_wildcard ccMail:"+fieldtext+"analyze_wildcard bccMail:"+fieldtext+"analyze_wildcard sentDate:"+fieldtext+"analyze_wildcard parentId:"+fieldtext+"analyze_wildcard parentType:"+fieldtext+"analyze_wildcard typeId:"+fieldtext+" &size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=toMail:"+fieldtext+"AND fromMail:"+fieldtext+"AND sentDate:"+fieldtext+"AND parentId:"+fieldtext+"AND parentType:"+fieldtext+"AND typeId:"+fieldtext+" &size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";

	String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=toMail:"+fieldtext+"AND fromMail:"+fieldtext+"AND ccMail:"+fieldtext+"AND bccMail:"+fieldtext+"AND sentDate:"+fieldtext+"AND parentId:"+fieldtext+"AND parentType:"+fieldtext+"AND typeId:"+fieldtext+" &size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	System.out.println("ElasticSearch Data URL >> "+url);
	String jsonElasticUrl=restTemplate.getForObject(url, String.class);
	 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
	 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
	return jsonElasticUrl;	
}
catch (Exception e) {
	System.out.println(e.getMessage());
}
return null;
	
}


//ONly blobid
@RequestMapping(value="/blobUrl")
public @ResponseBody String blobIdSearch(@RequestParam("blobId")String blobId){
	try{//
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=fromMail:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//specific field query
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=blobId:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//Any field query
		if(blobId.contains("@"))
			blobId=blobId.replaceAll("@", "%40");
		if(blobId.contains(":"))
			blobId=blobId.replaceAll(":", "%3A");
		String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=blobId:"+blobId+"&size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		System.out.println("ElasticSearch Data URL >> "+url);
		
		String jsonElasticUrl=restTemplate.getForObject(url, String.class);
		 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
		 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
		return jsonElasticUrl;	
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
	return null;
}

//ExactSearch for Subject
@RequestMapping(value="/suburl")
public @ResponseBody String subjectSearch(@RequestParam("subject")String subject){
	try{//
		String url =null;
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=fromMail:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//specific field query
		//String url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=blobId:"+name+"&size=1&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
		//Any field query
		if(subject.contains("@"))
			subject=subject.replaceAll("@", "%40");
		if(subject.contains(":"))
			subject=subject.replaceAll(":", "%3A");
		if(subject.contains("''"))
			subject=subject.replaceAll("''", "%27");
		/*if(subject.contains(" "))
			subject=subject.replaceAll(" ", "%20");*/
		subject.toLowerCase();
		//  String urlForDotnet=rootconfig.getResumeDataLive()+"/select?q=candidate_name : +\""+canName+"\"~1&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true&rows=5";

			 url="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?q=subject:+\" "+subject.toLowerCase()+"\"&size=10&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";
	
		
		System.out.println("ElasticSearch Data URL >> "+url);
		
		String jsonElasticUrl=restTemplate.getForObject(url, String.class);
		 System.out.println("jsonURL ElasticSearch Data >>  "+jsonElasticUrl);
		 //String jsonDataOffer=restTemplate.postForObject(offerMsURL, "http://localhost:8080/HitechReport/attachjid?jobid=66c5c563-7167-4e43-8f82-417196e1ea15", String.class);
		return jsonElasticUrl;	
	}
	catch (Exception e) {
		System.out.println(e.getMessage());
	}
	return null;
}





//String offerMsURL="https://search-hitech-tuxhxhzzt4sw2d5sak3l6e2mny.ap-south-1.es.amazonaws.com/emaildataindex/_search?default_operator=AND&q=typeId:other+hitech&size=20&from=0&pretty=true&_source=subject,fromMail,toMail,ccMail,bccMail,sentDate,typeId,volumeId,blobId,parentId,parentType";


//end HitechReport-------------------------------------------------//



/////////////------------------for using SpringRest POST API 












//For3cXAPI check then work...
/*@RequestMapping(value="/for3CXLive")
public @ResponseBody String for3CX(@RequestParam("dataForCx") String dataForCx) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException, JSONException{
	 String jsondata="";
	dataForCx=URLEncoder.encode(dataForCx,"UTF-8");
	
	if(dataForCx.contains("%40")){
		dataForCx=dataForCx.replaceAll("%40", "@");
		
		String jsonData=rootconfig.getContactMs_shardUrl()+"/select?q=(workingmailid:"+dataForCx.toLowerCase()+")&fl=id,salutation,firstname,midlename,lastname,moduleName&wt=json&rows=1&omitHeader=true&indent=true";		
		//System.out.println("jsonData::::"+jsonData);	
			URL url = new URL(jsonData);
		    String nullFragment = null;			         
		    URI UOrig = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),url.getQuery(), nullFragment);
		    System.out.println("Original Url:" + UOrig);
		    jsondata = restTemplate.getForObject(UOrig, String.class);
	
	}else{
		String    jsondata1=rootconfig.getContactMs_shardUrl()+"/select?q=(mobilenumber:"+dataForCx+" OR officephone:"+dataForCx+")&fl=id,salutation,firstname,midlename,lastname,moduleName&wt=json&rows=1&omitHeader=true&indent=true";
		   
		    jsondata = restTemplate.getForObject(jsondata1, String.class);
	}
	
	JSONObject jsonObject1 = (JSONObject) new JSONObject(jsondata).get("response");			
	//System.out.println(jsondata);
	Integer total = (Integer) jsonObject1.get("numFound");
	System.out.println(" Total Data from Contact:::: "+total);
	
	if(total==0){
		dataForCx= "\""+dataForCx+"\"";
		System.out.println("emailid::"+dataForCx);
		//JSONObject jsonobj1 = new JSONObject();	
		String jsonData="";
		//try{
		String url4ComapnyDetails="";
		//7-15-17
		//if(dataForCx.contains(" ")){		
		//if(dataForCx.equals("EmailID")){
		if(dataForCx.contains("@")){
			dataForCx=dataForCx.replaceAll("%40", "@");
			System.out.println("EmailId in candidate block::::"+dataForCx);
			url4ComapnyDetails=rootconfig.getCandidateMs_shardUrl()+"/select?q=EmailID:"+dataForCx+"&fl=id,candidate_name&wt=json&rows=1&omitHeader=true&indent=true";
		}else{
			System.out.println("Hi candidate else block::::"+dataForCx);
			url4ComapnyDetails=rootconfig.getCandidateMs_shardUrl()+"/select?q=Mobile:"+dataForCx+"&fl=id,candidate_name&wt=json&rows=1&omitHeader=true&indent=true";
	    }
		
		if(url4ComapnyDetails.contains(" ") ||url4ComapnyDetails.contains(",")) {
			url4ComapnyDetails = url4ComapnyDetails.replace(" ", "+");
			System.out.println("url4ComapnyDetails for iS>>>"+url4ComapnyDetails);
		}	
		
		 URL url4ComapnyDetails1 = new URL(url4ComapnyDetails);
		 String nullFragment = null;

		  URI UOrig = new URI(url4ComapnyDetails1.getProtocol(), url4ComapnyDetails1.getUserInfo(), url4ComapnyDetails1.getHost(), url4ComapnyDetails1.getPort(), url4ComapnyDetails1.getPath(),
		  url4ComapnyDetails1.getQuery(), nullFragment);

		  jsondata = restTemplate.getForObject(UOrig, String.class);
		  //System.out.println("jsonData::::"+jsondata);
	}
	
	
	return jsondata;
	
}
*/



///end 3cxAPI





@RequestMapping(value="/candidatee")
//public @ResponseBody String getDetails(@RequestParam("search") String search, @RequestHeader HttpHeaders headers) throws URISyntaxException, MalformedURLException{
public @ResponseBody String getDetails(@RequestParam("search") String search) throws URISyntaxException, MalformedURLException{
	
	
	String	sort = "id" + " desc";
	//System.out.println("sort:::"+sort);
	if(search.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		search ="\"" +search +"\"";
		//search=search.replace("\"", "\"\"");
	}
	
	//System.out.println(headers.getContentLength());
	//System.out.println(headers);
	
	/*if(headers.containsKey("authorization")){
	 System.out.println("get autorization!!!");
	}else{
		System.out.println("No Autorization!!!");
	}
	
	
	
	if(!headers.containsKey("authorization")){
		System.out.println("No Authentication");
		return "{\"error\":\"Please Provide The Authentication\"}";
 	}
 
	 String authString = headers.getFirst("authorization");

	 if(!restService.isUserAuthenticated(authString)){
		   System.out.println("Wrong Authentication.....");
           return "{\"error\":\"User not authenticated\"}";
      }*/
	
	
	System.out.println("search::::"+search);
	//int start = 100 *(next);
	//System.out.println("start::::"+start);
	//String subUrlEmcod="http://192.168.1.95:8983/solr/htmldata_shard1_replica1/select?q=*&fl=id,candidate_name,EmailID,empId&start="+start+"&sort="+sort+"&rows=100&wt=json&indent=true";
	//String subUrlEmcod=rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start="+start+"&rows=100&omitHeader=true&wt=json&indent=true";
   // String rooturl= "http://naarad.globalhuntindia.com:8983/solr/Html_shard1_replica2/select?q=_text_:"+search+"&fl=id,current_work_loc,dob,PerferLocation,CreatedDate,EmailID,DesignationText,TotalExps,PresentEmployer,IndustryText,FunctionText,candidate_name,NoticePeriod,Mobile,CurrentCTC,hasdoc,LastUpdateDate,phychallenged,Gender,SkillsText,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,PrefferdLocation,portal,html_data,MonsterHtml_data&start=0&rows=100&omitHeader=false&wt=json&indent=true";
	// "http://localhost:8983/solr/jcg/select?q=cat:book&hl=true&hl.q=";
	// "&hl.fl=*&hl.simple.pre=<strong>&hl.simple.post=</strong>&wt=json";

	//hl=true&hl.snippets=1&hl.fl=*&hl.fragsize=0
			
			
String rooturl= rootconfig.getResumeDataLive()+"/select?q=_text_:"+search+"&hl.fl=candidate_name,SkillsText,&start=0&rows=100&hl=true&hl.snippets=1&hl.fragsize=0&hl.simple.pre=<strong>&hl.simple.post=</strong>&omitHeader=false&wt=json&indent=true";
	
			
			
	 //String rooturl= rootconfig.getResumeDataLive()+"/select?q=_text_:"+search+"&hl.q="+search+"&hl.fl=*&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&hl=true&hl.simple.pre=<mark>&hl.simple.post=</mark>&wt=json&indent=true";

   // String rooturl= rootconfig.getResumeDataLive()+"/select?q=_text_:"+search+"&fl=id,candidate_name,PresentEmployer,DesignationText,current_work_loc,NoticePeriod,IndustryText,FunctionText,SkillsText,Mobile,portal,TotalExps,CurrentCTC,EmailID,Gender,dob,graduate,emp_status,age,Address,empId,ugYear,ugInst,pgInst,previousEmployer,previousDesig,CreatedDate,CreatedUserID,LastUpdateDate&start=0&rows=100&omitHeader=true&wt=json&indent=true";
   
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
    System.out.println(rooturl);
    
    URL url = new URL(rooturl);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;
	
}

@RequestMapping(value="/linkedin")
public @ResponseBody String linkedinDetails(@RequestParam("id") String id) throws URISyntaxException, MalformedURLException{
	
	String	sort = "id" + " desc";
	
	//System.out.println("sort:::"+sort);
	if(id.length() >=1){
		//search ="\\"+ "\"" +search + "\\" +"\"";
		id="\"" +id +"\"";
		//search=search.replace("\"", "\"\"");
	}
	
	System.out.println("id::::"+id);
	//int start = 100 *(next);
	//System.out.println("start::::"+start);
	
	 String rooturl= rootconfig.getResumeDataLive()+"/select?q=_text_:"+id+"&fl=id,candidate_name,EmailID,PassportNo,Mobile,totalExps,DOB,AlternateEmailID,Gender,SkillsText,EducationText,CreatedUserID,CreatedDate,PerferLocation,LastUpdateDate,ViewCount,EmployeeID,ExpectedCTC,PresentCurrency,PresentEmployer,FunctionText,IndustryText,SubFunctionText,"
	 +"DesignationText,EmpID,PinCode,UserName,previous_company_detail,logo_Contact,linkedin_can_id&start=0&rows=100&omitHeader=true&wt=json&indent=true";
   
    //String rooturl=rootconfig.getResumeDataLive()+"/select?q=id:"+id+"&fl=id,html_data,portal,MonsterHtml_data&rows=100&omitHeader=false&wt=json&indent=true";
    System.out.println(rooturl);
    
    URL url = new URL(rooturl);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
    //System.out.println("jsonData:::"+jsonData);
	return jsonData;

}


@RequestMapping(value="/findemail")
public @ResponseBody String get(@RequestParam("email") String email ) throws URISyntaxException, MalformedURLException{
	//email=email.toLowerCase();
	//email="+"+email+"+";
	// String title ="</em>";
    
    email= "\""+email+"\"";
	System.out.println("email"+email);
	
	String subUrlEmcod=rootconfig.getResumeDataLocal()+"/select?q=(html_data:"+email.toLowerCase()+")&fl=id,candidate_name,empId,EmailID&rows=500&wt=json&omitHeader=true&indent=true";
    System.out.println(subUrlEmcod);
    
    URL url = new URL(subUrlEmcod);
    String nullFragment = null;

    URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
    url.getQuery(), nullFragment);
    
    String  jsonData=restTemplate.getForObject(U, String.class);
	
			return jsonData;
	
}




public static  boolean dochek4Null(String value) {
	boolean flag = false;
	if ( ""!=value) {
		flag = true;
	}
	//System.out.println("dochek4Null>>>>>" + flag);
	return flag;
}



}



/*if(url.contains(" ") ||url.contains(",")) {
url = url.replace(" ", "+");
//System.out.println("url>>>>"+url);
}*/


/*URL url = new URL(subUrlEmcod);
String nullFragment = null;

URI U = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
url.getQuery(), nullFragment);*/









/*OLD APIs ================= 24/07/2017================*/

/*@RequestMapping(value="/htmlDataCmpny")
public @ResponseBody String htmlData(@RequestParam("qname") String qname,@RequestParam("qedu") String qedu,@RequestParam("qeduyr") String qeduyr,@RequestParam("quninver") String quninver, @RequestParam("company") String company){
	String jsonData="";
	try{
	String title="<title>";
	String url="";
    qname= "\""+qname+"\"";
    
	qeduyr = "\"*" + qeduyr.replace(",", "*\" AND \"*") + "*\""; 
	
	quninver=quninver.toLowerCase();// change made on 7th Aug from vivek
	quninver = "\"*" + quninver.replace(",", "*\" AND \"*") + "*\"";
	qedu = "\"*" + qedu.replace(",", "*\" AND \"*") + "*\"";
	
	company=company.toLowerCase();// change made on 7th Aug from vivek
	company = "\"*" + company.replace(",", "*\" AND \"*") + "*\"";
	
	System.out.println("company name:::"+company);
	
	//7-15-17
	if(qname.contains(" ")){
		//System.out.println("with space!!!");  // hi sir here with name with OR but i change OR with AND.
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
   }else{
		//System.out.println("without space!!!");
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+" AND "+company+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
    }
	
	if(url.contains(" ") ||url.contains(",")) {
		url = url.replace(" ", "+");
		//System.out.println("url>>>>"+url);
	}	
	
	System.out.println("url for htmlData With Company::: "+url);
	jsonData=restTemplate.getForObject(url, String.class);
	}catch(Exception e){
    	e.printStackTrace();
    	String str= e.getMessage();
    	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
	}
	return jsonData;
	
	
}
*/	
	 /*==================== close===================*/


/*@RequestMapping(value="/htmlData")
public @ResponseBody String htmlData(@RequestParam("qname") String qname,@RequestParam("qedu") String qedu,@RequestParam("qeduyr") String qeduyr,@RequestParam("quninver") String quninver){
	String jsonData="";
	try{
	String title ="<title>";
	String url="";
   qname= "\""+qname+"\"";
   
  
   	qeduyr = "\"*" + qeduyr.replace(",", "*\" AND \"*") + "*\""; 
	
   	quninver=quninver.toLowerCase();// change made on 7th Aug from vivek
	quninver = "\"*" + quninver.replace(",", "*\" AND \"*") + "*\"";
	
	
	qedu = "\"*" + qedu.replace(",", "*\" AND \"*") + "*\"";
	//System.out.println("qname:::"+qname);
	
	// imp String url="http://192.168.1.237:8983/solr/resume1_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";
	
	//7777777String url=rootconfig.getResumeDataLocal()+"/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";

	// code comment by vivek 7th Aug
	//7-15-17
	if(qname.contains(" ")){
		
		
		//System.out.println("with space!!!");/ // hi sir here with name with OR but i change OR with AND.
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
  }else{
		//System.out.println("without space!!!");
	  
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
   }

	
	//7th Aug 2017 vivek
		if(qname.contains(" ")){			
			//System.out.println("with space!!!");/ // hi sir here with name with OR but i change OR with AND.
			 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
	  }else{
			//System.out.println("without space!!!");		  
			 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+title+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=true&indent=true";
	   }
   //String url="http://192.168.1.237:8983/solr/resume_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,html_data&wt=json&indent=true";
	
	if(url.contains(" ") ||url.contains(",")) {
		url = url.replace(" ", "+");
		//System.out.println("url>>>>"+url);
	}	
	
	System.out.println("url for htmlData without cmpny::: "+url);
	jsonData=restTemplate.getForObject(url, String.class);
	}catch(Exception e){
   	e.printStackTrace();
   	String str= e.getMessage();
   	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
	}
	return jsonData;
	
}*/


/*
 * imp 
 * 
 * 
 * @RequestMapping(value="/htmlData")
public @ResponseBody String htmlData(@RequestParam("qname") String qname,@RequestParam("qedu") String qedu,@RequestParam("qeduyr") String qeduyr,@RequestParam("quninver") String quninver){
	String jsonData="";
	try{
	
	String url="";
    qname= "\""+qname+"\"";
    
	qeduyr = "\"*" + qeduyr.replace(",", "*\" OR \"*") + "*\""; 
	
	quninver = "\"*" + quninver.replace(",", "*\" OR \"*") + "*\"";
	qedu = "\"*" + qedu.replace(",", "*\" OR \"*") + "*\"";
	//System.out.println("qname:::"+qname);
	
	// imp String url="http://192.168.1.237:8983/solr/resume1_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";
	
	//7777777String url=rootconfig.getResumeDataLocal()+"/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";

	//7-15-17
	if(qname.contains(" ")){
		//System.out.println("with space!!!");
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" OR html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig,html_data&wt=json&omitHeader=false&indent=true";
   }else{
		//System.out.println("without space!!!");
		 url=rootconfig.getResumeDataLocal()+"/select?q=(candidate_name:"+qname.toLowerCase()+" AND html_data:"+qname.toLowerCase()+") AND (html_data:"+qedu+" AND "+qeduyr+" AND "+quninver.toLowerCase()+")&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,empId,ugYear,pgYear,previousEmployer,previousDesig&wt=json&omitHeader=true&indent=true";
    }

    //String url="http://192.168.1.237:8983/solr/resume_shard1_replica1/select?q=html_data "+qname+" AND "+qedu+" AND "+qeduyr+" AND "+quninver+"&fl=id,candidate_name,DesignationText,TotalExp,PresentEmployer,PresentCTC,SkillsText,current_work_loc,graduate,postgraduate,html_data&wt=json&indent=true";
	
	if(url.contains(" ") ||url.contains(",")) {
		url = url.replace(" ", "+");
		//System.out.println("url>>>>"+url);
	}	
	
	System.out.println("url for htmlData::: "+url);
	jsonData=restTemplate.getForObject(url, String.class);
	}catch(Exception e){
    	e.printStackTrace();
    	String str= e.getMessage();
    	if (str.contains("400 Bad Request")) {
			return "{\"error\":\"data should not be blank\"}";
		}
	}
	return jsonData;
	
}*/



