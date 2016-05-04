
//define the main page module - which holds all containers
angular.module('couponSystem', []);

//for each screen-div define a dedicated controller. in this case we got only one screen...
angular.module('couponSystem').controller('toDoController', function ($rootScope, $scope, $http) {
    //this part is executed on page/controller load:

    $scope.showDiv = true;

    //get - use get method for your request
    //success - takes the function to execute when call is successful
    //response2 - is the object that results from your ajax call. If you returned a JSON - than this is the object unmarshalled from your json... 
    //$http.get("rest/admin/adminLogin?user=admin&pass=1234").success(function (response2) { $scope.task = response2; });
    //ofer   localhost:8080/CouponSystemWEB/rest/admin/adminLogin?user=admin&pass=1234
    ///////////

    //this part is executed with events:
    $scope.login = function () {

        //		if($scope.showDiv==false){
        alert("sdfsdf");
        //$http.get("rest/admin/adminLogin?user=admin&pass=1234");
        $http.get("rest/" + $scope.facade + "/" + $scope.facade + "Login?" + "user=" + $scope.user + "&pass=" + $scope.pass);
        $scope.resultLogin = true;
        $scope.resultString = "i made a login";
        //			$scope.showDiv=true;
        //		}else{
        //			$scope.showDiv=false;
        //		}
    };

    $scope.sendSummaryToServer = function () {
        $http.get("rest/jaxb/todo/process?toProcess=" + $scope.selectedSummary).success(function (response) { $rootScope.processResult = response });
    };

    //////////
});


angular.module('myApp').controller('otherController', function($scope, $http) {
	$scope.fontColor=Math.random();
});