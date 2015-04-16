<?php
$time=time();
while(time()-$time < 30){
    //$data="test";
    if(!empty($data)){
        echo "all ok";
        break;
    }
    usleep(25000);
}
?>