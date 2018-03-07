(function () {
    window['typeRecord'] = 'video';
    window['recording'] = false;
    window['paused'] = false;
})();

var recordVideo = function (publisherOptions) {
    document.getElementById('recording-div').style.display = 'block';
    document.getElementById('record-btn').style.display = 'none';
    disableRadioButtons(true);
    window['OV'] = new OpenVidu();
    window['publisher'] = window['OV'].initPublisher(
        'post-video',
        publisherOptions,
        function (err) {
            if (err) {
                console.warn(err);
                if (err.name === 'SCREEN_EXTENSION_NOT_INSTALLED') {
                    if (confirm("In Chrome you need an extension installed to share your screen. Click OK to install it")) {
                        window.location.href = err.message;
                    }
                }
            }
        }
    );
    window['publisher'].on('videoElementCreated', function (e) {
        if (publisherOptions.audio && !publisherOptions.video) {
            e.element.style.backgroundColor = '#4d4d4d';
            e.element.setAttribute('poster', 'assets/volume.png');
        }
    });
    window['publisher'].on('videoPlaying', function (e) {
        window['recordRadioEnabled'] = true;
        disableRadioButtons(false);
        document.getElementById('recording-video').style.display = 'block';
    });
}

var getPublisherOptions = function (option) {
    var options = {};
    switch (option) {
        case 'video':
            options = {
                audio: true,
                video: true,
                quality: 'MEDIUM'
            }
            break;
        case 'audio':
            options = {
                audio: true,
                video: false
            }
            break;
        case 'screen':
            options = {
                audio: true,
                video: true,
                quality: 'MEDIUM',
                screen: true
            }
            break;
    }
    return options;
}

var recordRadioChange = function (event) {
    var type = window['typeRecord'];
    if (event.value !== type) {
        window['typeRecord'] = event.value;
        cleanRecording();
        recordVideo(getPublisherOptions(event.value));
    }
}

var startStopRecording = function () {
    if (!window['recording']) {
        window['recorder'] = window['OV'].initLocalRecorder(window['publisher'].stream);
        window['recorder'].record();
        showRecordingControls();
        $('#record-start-stop').html('Finish');
        $('#record-pause-resume').show();
    } else {
        window['recorder'].stop()
            .then(() => {
                $('#post-video video').hide();
                $('#radio-buttons').hide();
                showUploadControls();
                var recordingPreview = window['recorder'].preview('post-video');
                recordingPreview.controls = true;
            })
            .catch((e) => {});
    }
    this.recording = !this.recording;
}

var pauseResumeRecording = function () {
    if (!window['paused']) {
        window['recorder'].pause();
        $('#record-pause-resume').html('Resume');
        $('#post-video video').get(0).pause();
    } else {
        window['recorder'].resume();
        $('#record-pause-resume').html('Pause');
        $('#post-video video').get(0).play();
    }
    this.paused = !this.paused;
}

var upload = function () {
    window['recorder'].uploadAsMultipartfile('/recording')
        .then(function (response) {
            console.log(response);
        })
        .catch(function (error) {
            console.error(error);
        });
}

var cleanRecording = function () {
    if (!!window['recorder']) window['recorder'].clean();
    if (!!window['publisher']) window['publisher'].destroy();
    var el = $('#post-video video');
    if (!!el) {
        el.remove();
    }
    document.getElementById('recording-video').style.display = 'none';
    window['recording'] = false;
    window['paused'] = false;
    showRecordingControls();
    $('#radio-buttons').show();
    $('#record-start-stop').html('Start');
    $('#record-pause-resume').html('Pause');
    $('#record-pause-resume').hide();
}

var disableRadioButtons = function (enable) {
    let radios = document.getElementsByName('record-radio');
    radios.forEach(function (el) {
        el.disabled = enable;
    });
}

var showRecordingControls = function () {
    $('#recording-controls').show();
    $('#upload-controls').hide();
}

var showUploadControls = function () {
    $('#recording-controls').hide();
    $('#upload-controls').show();
}


var reinit = function () {
    window['typeRecord'] = 'video';
    $('#record-video').prop('checked', true);
    $('#recording-div').hide();
    $('#record-btn').show();
}

var navigateTo = function (page) {
    if (page === 'download') {
        cleanRecording();
        reinit();
        $('#download').show();
        $('#upload').hide();
        $('#nav-download').addClass('active');
        $('#nav-download a').addClass('active');
        $('#nav-upload').removeClass('active');
        $('#nav-upload a').removeClass('active');
    } else {
        $('#download').hide();
        $('#upload').show();
        $('#nav-upload').addClass('active');
        $('#nav-upload a').addClass('active');
        $('#nav-download').removeClass('active');
        $('#nav-download a').removeClass('active');
    }
}

var updateInput = function(val) {
    $('#videoId').val(val);
}

var getVideo = function () {
    var id = $('#videoId').val();
    $('#get-video').get(0).src = '/recording/' + id;
}

var listAllVideos = function () {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var list = $("#list-files");
            list.empty();
            var files = JSON.parse(xmlHttp.responseText);
            files.forEach(function(file) {
                list.append("<div onclick=\"updateInput('" + file + "')\">" + file + "</div>");
            });
        }
    }
    xmlHttp.open("GET", "/recording/all", true); // true for asynchronous 
    xmlHttp.send();
}