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
        if check.success? and (not check.result.oauth) and check.result.authenticate(password)
            user = check.result
            
            # Delete all tokens correlated to candidate user
            list_token = BlacklistedToken.where(user_id: user.id)
            for el in list_token
                BlacklistedToken.destroy(el.id)
            end

            # Delete the user
            User.destroy(user.id)
        else
            errors.add(:password, 'Wrong password')
        end
    end
end