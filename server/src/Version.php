<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Version
 *
 * @author cotes
 */
namespace Tyrell;

use Tonic\Resource,
    Tonic\Response,
    Tonic\Request,
    Tonic\ConditionException;

/**
 * @uri /version
 * @uri /version/([0-9]*['.'0-9]*)
 */
class Version extends Resource
{

    /**
     *
     * @method GET
     * @provides application/json
     * @json
     * @return Tonic\Response
     */
    public function getVersion()
    {
        
        $version = $this->getCurrentVersion();
	if ($version instanceof Response) {
	    return $version;
	}        
	//echo $version;
        return new Response(200, array(
            'version' => $version
        ));
    }
    
    private function getCurrentVersion() {
        $file = "./src/version";
        $fd = fopen($file, 'r');
        
        if (! $fd) {
            return new Response(Response::INTERNALSERVERERROR, 
                    "Unable to open version file.");
        }
	
	$data = fread($fd, filesize($file)-1);
        if (! $data){
            return new Response(Response::INTERNALSERVERERROR, 
                    "Unable to read file.");
        }
        fclose($fd);
	//echo $data;
	//$data = strstr($data, '\n', true);
	//echo $data;
        return $data;
    }
    
    /**
     * Condition method to turn output into JSON
     */
    protected function json()
    {
        $this->before(function ($request) {
            if ($request->contentType == "application/json") {
                $request->data = json_decode($request->data);
            }
        });
        $this->after(function ($response) {
            $response->contentType = "application/json";
            if (isset($_GET['jsonp'])) {
                $response->body = $_GET['jsonp'].'('.json_encode($response->body).');';
            } else {
                $response->body = json_encode($response->body);
            }
        });
    }
}
?>
