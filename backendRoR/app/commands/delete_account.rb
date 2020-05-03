class DeleteAccount
    prepend SimpleCommand

    def initialize(headers = {}, password)
        @headers = headers
        @password = password
    end
  
    def call
        deleteaccount
    end

    private

    attr_accessor :headers

    def deleteaccount
        check = AuthorizeApiRequest.call(headers)
        if check.success? 
            # local account
            if (not check.result.oauth) 
                if check.result.authenticate(@password)
                    user = check.result
                    
                    destroyaccount(user)
                else
                    errors.add(:password, 'Wrong password')
                end
            # oauth account
            else
                if get_info_token_oauth
                    user = check.result

                    destroyaccount(user)
                    deletependingbooking(user)
                else
                    errors.add(:password, 'Wrong oauth token')
                end
            end
        else
            errors.add(:auth_token, 'Token not valid')
        end
    end

    # destroy account
    def destroyaccount(user)
        # Delete all tokens correlated to candidate user
        list_token = BlacklistedToken.where(user_id: user.id)
        for el in list_token
            BlacklistedToken.destroy(el.id)
        end

        # Delete the user
        User.destroy(user.id)
    end 

    # delete pending booking in firebase to avoid locker deadlock
    def deletependingbooking(user)
        body = JSON.parse(HTTParty.get('https://firestore.googleapis.com/v1beta1/projects/'+ENV["FIREBASE"]+'/databases/(default)/documents/bookings').body)

        for el in body["documents"]
            # pending booking
            if el["fields"]["user"]["stringValue"] == user.email and el["fields"]["active"]["booleanValue"]
                name = el["name"]
                city = el["fields"]["city"]["stringValue"]
                park = el["fields"]["park"]["stringValue"]
                lockHash = el["fields"]["lockHash"]["stringValue"]

                # free locker
                new_body = JSON.parse(HTTParty.get('https://firestore.googleapis.com/v1beta1/projects/'+ENV["FIREBASE"]+'/databases/(default)/documents/cities/'+hash_code(city).to_s+'/parks/'+hash_code(city+park).to_s+'/lockers/'+lockHash).body)
                
                new_body.delete("createTime")   #useless field
                new_body.delete("updateTime")   #useless field
                new_body["fields"]["user"]["stringValue"] = '""'
                new_body["fields"]["available"]["booleanValue"] = true
                new_body["fields"]["open"]["booleanValue"] = false
                
                new_body = JSON.generate(new_body)
                HTTParty.patch('https://firestore.googleapis.com/v1beta1/projects/'+ENV["FIREBASE"]+'/databases/(default)/documents/cities/'+hash_code(city).to_s+'/parks/'+hash_code(city+park).to_s+'/lockers/'+lockHash, 
                                body: JSON.generate(new_body), headers: {"Content-Type": "application/json"} )

                # delete booking
                HTTParty.delete('https://firestore.googleapis.com/v1beta1/'+name)
            end
        end
    end

    # in case of oauth account
    def get_info_token_oauth
        response = HTTParty.get('https://graph.facebook.com/debug_token?input_token='+@password+'&access_token='+ENV["APP_ID_FACEBOOK"]+'|'+ENV["SECRET_KEY_FACEBOOK"])
        if response.code == 200
            body = JSON.parse(response.body)
            if body['data']['is_valid']
                return body['data']['user_id']
            else
                errors.add(:token, 'Token not valid')
            end
        else
            errors.add(:token, 'Token not valid')
        end
        nil
    end

    # Function used to compute Java String hash
    def hash_code(str)
        str.each_char.reduce(0) do |result, char|
          [((result << 5) - result) + char.ord].pack('L').unpack('l').first
        end
    end

end