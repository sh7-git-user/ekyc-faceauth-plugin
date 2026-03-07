var exec = require('cordova/exec');

exports.faceAuth = function (salt, success, error) {

    if (!salt) {
        if (error) {
            error("Salt is required");
        }
        return;
    }

    exec(
        success,
        error,
        "FaceAuthPlugin",
        "faceAuth",
        [salt]
    );
};
