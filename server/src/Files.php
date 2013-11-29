<?php
namespace Tyrell;

use Tonic\Resource,
    Tonic\Response,
    Tonic\Request,
    Tonic\ConditionException;

/**
 * @uri /files
 */
class Files extends Resource
{
    /**
    * @method GET
    * @param Request request
    * @return Response
    */
    public function getFiles()
    {
	$myFile = "/var/www/api/src/stacksync.zip";
	$fh = fopen($myFile, 'r');
        if (! $fh) {
            return new Response(Response::INTERNALSERVERERROR, 
                    "Unable to open file.");
        }
        
	$theData = fread($fh, filesize($myFile));
        if (! $theData){
            return new Response(Response::INTERNALSERVERERROR, 
                    "Unable to read file.");
        }
	fclose($fh);
        
        $response = new Response(Response::OK, $theData);
        
        $md5 = md5_file($myFile);
        if (!$md5){
            return new Response(Response::INTERNALSERVERERROR, 
                    "Unable to craete MD5 file.");
        }
        
        $response->md5 = $md5;
        return $response;
    }
    
}

?>
